/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2014 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.ServiceProvider;

import java.util.ArrayList;
import java.util.Set;

public class BTConnectDeviceActivity extends Activity {

	public static final String TAG = BTConnectDeviceActivity.class.getSimpleName();

	public static final String SERVICE_TO_START = "org.catrobat.catroid.bluetooth.SERVICE";
	public static final String AUTO_CONNECT = "auto_connect";

	private static final int DEVICE_MAC_ADDRESS_LENGTH = 18;

	private static ArrayList<String> autoConnectIDs;
	private static BTDeviceFactory btDeviceFactory;

	protected BTDeviceService deviceService;

	private BluetoothManager btManager;

	private ArrayAdapter<String> pairedDevicesArrayAdapter;
	private ArrayAdapter<String> newDevicesArrayAdapter;
	private boolean autoConnect;

	private static final String OUI_LEGO = "00:16:53";

	static {
		autoConnectIDs = new ArrayList<String>();
		autoConnectIDs.add(OUI_LEGO);
	}

	public static void setDeviceFactory(BTDeviceFactory deviceFactory) {
		btDeviceFactory = deviceFactory;
	}

	private OnItemClickListener deviceClickListener = new OnItemClickListener() {

		private String getSelectedBluetoothAddress(View view) {
			String info = ((TextView) view).getText().toString();
			if (info.lastIndexOf('-') != info.length() - DEVICE_MAC_ADDRESS_LENGTH) {
				return null;
			}

			return info.substring(info.lastIndexOf('-') + 1);
		}

		@Override
		public void onItemClick(AdapterView<?> av, View view, int position, long id) {

			String address = getSelectedBluetoothAddress(view);
			if (address == null) {
				return;
			}
			connectDevice(address);
		}
	};

	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if ((device.getBondState() != BluetoothDevice.BOND_BONDED)) {
					newDevicesArrayAdapter.add(device.getName() + "-" + device.getAddress());
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.select_device + " " + deviceService.getName());
				if (newDevicesArrayAdapter.isEmpty()) {
					String noDevices = getResources().getString(R.string.none_found);
					newDevicesArrayAdapter.add(noDevices);
				}
			}
		}
	};

	private class ConnectDeviceTask extends AsyncTask<String, Void, BluetoothConnection.State> {

		BluetoothConnection btConnection;
		private ProgressDialog connectingProgressDialog;

		@Override
		protected void onPreExecute() {
			setVisible(false);
			connectingProgressDialog = ProgressDialog.show(BTConnectDeviceActivity.this, "",
					getResources().getString(R.string.connecting_please_wait), true);
		}

		@Override
		protected BluetoothConnection.State doInBackground(String... adresses) {
			btConnection = btDeviceFactory.createBTConnectionForDevice(deviceService.getServiceType(), adresses[0],
					deviceService.getBluetoothDeviceUUID(), BTConnectDeviceActivity.this.getApplicationContext());

			return btConnection.connect();
		}

		@Override
		protected void onPostExecute(BluetoothConnection.State connectionState) {

			connectingProgressDialog.dismiss();

			int result = RESULT_CANCELED;

			if (connectionState == BluetoothConnection.State.CONNECTED) {
				deviceService.setConnection(btConnection);
				result = RESULT_OK;
				BTDeviceConnector btDeviceConnector = ServiceProvider.getService(BTDeviceConnector.class);
				btDeviceConnector.deviceConnected(deviceService);
			}
			else if (autoConnect) {
				Log.i(TAG, "auto connect wasn't successful, show available devices instead.");
				Toast.makeText(BTConnectDeviceActivity.this, R.string.bt_auto_connection_failed, Toast.LENGTH_SHORT).show();
				BTConnectDeviceActivity.this.setVisible(true);
				autoConnect = false;
				return;
			}
			else {
				Toast.makeText(BTConnectDeviceActivity.this, R.string.bt_connection_failed, Toast.LENGTH_SHORT).show();
			}

			setResult(result);
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		autoConnect = this.getIntent().getExtras().getBoolean(AUTO_CONNECT);
		if (autoConnect) {
			this.setVisible(false);
		}

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);
		setTitle(R.string.select_device);

		setResult(Activity.RESULT_CANCELED);

		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				doDiscovery();
				view.setVisibility(View.GONE);
			}
		});

		pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		newDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(pairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(deviceClickListener);

		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(newDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(deviceClickListener);

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(receiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(receiver, filter);

		int bluetoothState = activateBluetooth();
		if (bluetoothState == BluetoothManager.BLUETOOTH_ALREADY_ON) {
			listAndSelectDevices();
		}
	}

	private void listAndSelectDevices() {

		createAndSetDeviceService();

		Set<BluetoothDevice> pairedDevices = btManager.getBluetoothAdapter().getBondedDevices();

		BluetoothDevice bluetoothDevice = null;
		int possibleConnections = 0;
		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				for (String item : autoConnectIDs) {
					if (device.getAddress().startsWith(item)) {
						bluetoothDevice = device;
						possibleConnections++;
					}
				}
				pairedDevicesArrayAdapter.add(device.getName() + "-" + device.getAddress());
			}
		}

		if (pairedDevices.size() == 0) {
			String noDevices = getResources().getText(R.string.none_paired).toString();
			pairedDevicesArrayAdapter.add(noDevices);
		}

		if (autoConnect && possibleConnections == 1) {
			connectDevice(bluetoothDevice.getAddress());
		}
		else {
			this.setVisible(true);
		}
	}

	protected void createAndSetDeviceService() {
		Class<BTDeviceService> serviceType = (Class<BTDeviceService>)getIntent().getSerializableExtra(SERVICE_TO_START);

		if (btDeviceFactory == null) {
			btDeviceFactory = new BTDeviceFactoryImpl();
		}

		deviceService = btDeviceFactory.createDevice(serviceType, this.getApplicationContext());
	}

	private void connectDevice(String address) {
		btManager.getBluetoothAdapter().cancelDiscovery();
		new ConnectDeviceTask().execute(address);
	}

	@Override
	protected void onDestroy() {
		if (btManager != null && btManager.getBluetoothAdapter() != null) {
			btManager.getBluetoothAdapter().cancelDiscovery();
		}

		this.unregisterReceiver(receiver);
		super.onDestroy();
	}

	private void doDiscovery() {

		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);

		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

		if (btManager.getBluetoothAdapter().isDiscovering()) {
			btManager.getBluetoothAdapter().cancelDiscovery();
		}

		btManager.getBluetoothAdapter().startDiscovery();
	}

	private int activateBluetooth() {

		btManager = new BluetoothManager(this);

		int bluetoothState = btManager.activateBluetooth();
		if (bluetoothState == BluetoothManager.BLUETOOTH_NOT_SUPPORTED) {
			Toast.makeText(this, R.string.notification_blueth_err, Toast.LENGTH_LONG).show();
			setResult(Activity.RESULT_CANCELED);
			finish();
		}

		return bluetoothState;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("bt", "Bluetooth activation activity returned");

		switch (resultCode) {
			case Activity.RESULT_OK:
				listAndSelectDevices();
				break;
			case Activity.RESULT_CANCELED:
				Toast.makeText(this, R.string.notification_blueth_err, Toast.LENGTH_LONG).show();
				setResult(Activity.RESULT_CANCELED);
				finish();
				break;
		}
	}
}
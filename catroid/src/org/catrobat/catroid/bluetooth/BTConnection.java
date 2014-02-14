/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.catrobat.catroid.stage.StageObserver;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class BTConnection extends StageObserver {

	private BluetoothAdapter btAdapter = null;
	private BluetoothDevice btDevice = null;
	private BluetoothSocket btSocket = null;
	private String macAddress;
	private UUID uiid;

	// don't use this UUID in Production, NXT uses it, check other Projects (Albert, Ardoino..) and use similar UUID
	private static final UUID SERIAL_PORT_SERVICE_CLASS_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public static enum States {
		CONNECTED, NOT_CONNECTED, ERROR_ADAPTER, ERROR_SOCKET, ERROR_BONDING, ERROR_CLOSING
	}

	public BTConnection(String macAddress) {
		this(macAddress, SERIAL_PORT_SERVICE_CLASS_UUID);
	}

	public BTConnection(String macAddress, UUID uiid) {
		this.macAddress = macAddress;
		this.uiid = uiid;
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public States connect() {
		try {

			if (btAdapter.getState() != BluetoothAdapter.STATE_ON) {
				//sendToast(resources.getString(R.string.no_paired_nxt));
				return States.ERROR_ADAPTER;
			}

			btDevice = btAdapter.getRemoteDevice(macAddress);
			if (btDevice == null) {
				errorToasts(States.ERROR_SOCKET);
				return States.ERROR_SOCKET;
			}

			btSocket = btDevice.createRfcommSocketToServiceRecord(uiid);
			try {
				btSocket.connect();
			} catch (IOException ioEx) {
				if (btDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
					errorToasts(States.ERROR_BONDING);
					ioEx.printStackTrace();
					return States.ERROR_BONDING;
				}

				// try another method for connection, this should work on the HTC desire, credits to Michael Biermann
				try {
					Method mMethod = btDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
					btSocket = (BluetoothSocket) mMethod.invoke(btDevice, Integer.valueOf(1));
					btSocket.connect();
					return States.CONNECTED;
				} catch (Exception e1) {
					e1.printStackTrace();
					return States.ERROR_SOCKET;
				}
			}
		} catch (IOException e) {

			if (btDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
				errorToasts(States.ERROR_BONDING);
				e.printStackTrace();
				return States.ERROR_BONDING;
			}
			return States.ERROR_SOCKET;
		}
		return States.CONNECTED;
	}

	public void disconnect() {
		try {
			if (btSocket != null) {
				btSocket.close();
				btSocket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public BluetoothDevice getBTDevice() {
		return btDevice;
	}

	public BluetoothSocket getBTSocket() {
		return btSocket;
	}

	@Override
	public void onStageCreate() {
	}

	@Override
	public void onStagePause() {
	}

	@Override
	public void onStageResume() {
	}

	@Override
	public void onStageDispose() {
		disconnect();
	}

	protected void errorToasts(States state) {
		switch (state) {
			case ERROR_ADAPTER:
				//Toast.makeText(this, resources.getString(R.string.no_paired_nxt), Toast.LENGTH_LONG).show();
				break;
			case ERROR_SOCKET:
				//sendToast(resources.getString(R.string.no_paired_nxt));
				break;
			case ERROR_BONDING:
				//sendToast(resources.getString(R.string.pairing_message));
				break;
		}
	}
}

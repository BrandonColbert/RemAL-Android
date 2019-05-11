package exn.database.remal.events;

import exn.database.remal.deck.ITile;
import exn.database.remal.devices.IRemoteDevice;

public class DeviceTileCreateEvent extends DeviceEvent {
	public final ITile tile;
	public DeviceTileCreateEvent(IRemoteDevice device, ITile tile) {
		super(device);
		this.tile = tile;
	}
}

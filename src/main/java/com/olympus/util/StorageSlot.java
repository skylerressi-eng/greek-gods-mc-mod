package com.olympus.util;

/** Duck interface: slots that belong to a storage block/entity (chests, barrels, hoppers...). */
public interface StorageSlot {
	boolean olympus$isStorage();

	void olympus$setStorage(boolean storage);
}

package me.jellysquid.mods.radon.common.db.lightning;

import org.lwjgl.util.lmdb.MDBEnvInfo;

public class EnvInfo {
    public final long mapSize;

    public EnvInfo(MDBEnvInfo info) {
        // TODO: implement other fields
        this.mapSize = info.me_mapsize();
    }
}

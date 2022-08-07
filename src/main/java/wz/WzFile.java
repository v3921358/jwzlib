/*
	This file is part of JWzLib: MapleStory WZ File Parser
	Copyright (C) 2019  Brenterino <brent@zygon.dev>

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package wz;

import java.util.Map;
import java.util.Objects;

import wz.common.WzHeader;
import wz.io.WzInputStream;

/**
 * @author Brenterino
 */
public final class WzFile extends WzObject<WzFile, WzObject<?, ?>> {

    private String name;
    private short version;
    private WzDirectory root;

    public WzFile(String wz, short ver) {
        name = wz;
        version = ver;
    }

    @Override
    public void parse(WzInputStream in) {
        WzHeader header = new WzHeader();
        header.IDENT = in.readStringByLen(4);
        header.FILE_SIZE = in.readInteger();
        in.skip(4); // just going to be 0
        header.FILE_START = in.readInteger();
        header.COPYRIGHT = in.readStringByLen(header.FILE_START - 17);
        in.setHeader(header);
        int encryptVersion = in.readShort();
        int detectedVersion = getRealVersionHash(encryptVersion);
        if (detectedVersion != -1) {
            in.setHash(getVersionHash(detectedVersion));
        } else {
            in.setHash(getVersionHash(version));
        }
        in.setHash(getVersionHash(version));
        root = new WzDirectory(name, header.FILE_START + 2, header.FILE_SIZE, 0);
        root.parse(in);
    }

    public int getVersionHash(int ver) {
        int ret = 0;
        String v = String.valueOf(ver);
        for (int i = 0; i < v.length(); i++) {
            ret *= 32;
            ret += (int) v.charAt(i);
            ret += 1;
        }
        return ret & 0xFFFFFFFF;
    }

    private int getRealVersionHash(int encryptVersion) {
        for (int realVersion = 0; realVersion < 32767; realVersion++) {
            int versionHash = 0;
            int decryptedVersionNumber;
            int a, b, c, d, l;
            char[] versionNumberStr = String.valueOf(realVersion).toCharArray();
            l = versionNumberStr.length;
            for (int i = 0; i < l; i++) {
                versionHash = (32 * versionHash) + versionNumberStr[i] + 1;
            }
            a = (versionHash >> 24) & 0xFF;
            b = (versionHash >> 16) & 0xFF;
            c = (versionHash >> 8) & 0xFF;
            d = versionHash & 0xFF;
            decryptedVersionNumber = (0xff ^ a ^ b ^ c ^ d);

            if (encryptVersion == decryptedVersionNumber) {
                return realVersion;
            }
        }
        return -1;
    }

    public WzDirectory getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, WzObject<?, ?>> getChildren() {
        return root.getChildren();
    }

    @Override
    public void addChild(WzObject<?, ?> o) {
    }

    @Override
    public int compareTo(WzFile o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WzFile) {
            WzFile other = (WzFile) o;
            return other.name.equals(name) && other.version == version;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + this.version;
        return hash;
    }
}

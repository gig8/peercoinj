/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gig8.motacoinj.params;

import com.gig8.motacoinj.core.NetworkParameters;
import com.gig8.motacoinj.core.Sha256Hash;
import com.gig8.motacoinj.core.Utils;

import static com.google.common.base.Preconditions.checkState;

/**
 * Parameters for the main production network on which people trade goods and services.
 */
public class MainNetParams extends NetworkParameters {
    public MainNetParams() {
        super();
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(504365055L);
        dumpedPrivateKeyHeader = 183; // TU ??
        addressHeader = 55; // TU ??
        p2shHeader = 117; // TU ??
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        port = 17420;
        packetMagic= 0x304a304a;
        genesisBlock.setDifficultyTarget(504365055L);
        genesisBlock.setTime(1521404888L);
        genesisBlock.setNonce(145590L);
        id = ID_MAINNET;
        spendableCoinbaseDepth = 10;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("00000fea25f87416682baa54946a1d156909d0959588eb573a0ae16a64230c61"), genesisHash);
        checkpoints.put(25, new Sha256Hash("000001cc753748395adc5bada031b8df33ab40ac6cbff52babef0c56ebdc0bc3"));

        dnsSeeds = new String[] {
            "explorer.motacoin.vip",
        };
    }

    private static MainNetParams instance;
    public static synchronized MainNetParams get() {
        if (instance == null) {
            instance = new MainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_MAINNET;
    }

    @Override
    public String toString() {
        return "MotaCoin";
    }

}

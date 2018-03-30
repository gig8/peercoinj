/**
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
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

package com.gig8.motacoinj.core;

import com.gig8.motacoinj.params.*;
import com.gig8.motacoinj.script.Script;
import com.gig8.motacoinj.script.ScriptOpCodes;
import com.google.common.base.Objects;

import javax.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

import static com.google.common.base.Preconditions.checkState;
import static com.gig8.motacoinj.core.Coin.*;

/**
 * <p>NetworkParameters contains the data needed for working with an instantiation of a MotaCoin chain.</p>
 *
 * <p>This is an abstract class, concrete instantiations can be found in the params package. There are four:
 * one for the main network ({@link MainNetParams}), one for the public test network, and two others that are
 * intended for unit testing and local app development purposes. Although this class contains some aliases for
 * them, you are encouraged to call the static get() methods on each specific params class directly.</p>
 */
public abstract class NetworkParameters implements Serializable {
    /**
     * The protocol version this library implements.
     */
    public static final int PROTOCOL_VERSION = 60013; // TU src/version.h:static const int PROTOCOL_VERSION = 60013;

    /**
     * The alert signing key.
     */
    public static final byte[] SATOSHI_KEY = Utils.HEX.decode("04fbf80ea797ebcced8fc6efcc3cdf6681f443e90ed1ec345a9c33739ee4a1f6adaba440e6cfcc48e2e49b6a85453f526695f97e84fc1704cc50b74f15981a5b30");

    /** The string returned by getId() for the main, production network where people trade things. */
    public static final String ID_MAINNET = "org.motacoin.production";
    /** Unit test network. */
    public static final String ID_UNITTESTNET = "com.gig8.motacoinj.unittest";

    /** The string used by the payment protocol to represent the main net. */
    public static final String PAYMENT_PROTOCOL_ID_MAINNET = "main";

    protected Block genesisBlock;
    protected BigInteger maxTarget;
    protected int port;
    protected long packetMagic;
    protected int addressHeader;
    protected int p2shHeader;
    protected int dumpedPrivateKeyHeader;
    protected int interval;
    protected int targetTimespan;
    protected byte[] alertSigningKey;

    /**
     * See getId(). This may be null for old deserialized wallets. In that case we derive it heuristically
     * by looking at the port number.
     */
    protected String id;

    /**
     * The depth of blocks required for a coinbase transaction to be spendable.
     */
    protected int spendableCoinbaseDepth;
    
    protected int[] acceptableAddressCodes;
    protected String[] dnsSeeds;
    protected Map<Integer, Sha256Hash> checkpoints = new HashMap<Integer, Sha256Hash>();

    protected NetworkParameters() {
        alertSigningKey = SATOSHI_KEY;
        genesisBlock = createGenesis(this);
    }

    private static Block createGenesis(NetworkParameters n) {
        Block genesisBlock = new Block(n);
        Transaction t = new Transaction(n);
        t.setTime(1521404888);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "POTUS Tweets:'And yet, there is NO COLLUSION!'"
            byte[] bytes = Utils.HEX.decode
                    ("00012a2e504f545553205477656574733a27416e64207965742c207468657265206973204e4f20434f4c4c5553494f4e2127");
            t.addInput(new TransactionInput(n, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            t.addOutput(new TransactionOutput(n, t, ZERO, scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }
        genesisBlock.addTransaction(t);
        
        String merkleHash = genesisBlock.getMerkleRoot().toString();
        checkState(merkleHash.equals("b3d8b0c93a5a36fc81b2cdc8a5ca2adb781f4d22530cd4671cf2e2c8181754d6"), merkleHash);
        
        return genesisBlock;
    }

    public static final int TARGET_TIMESPAN = 60 * 60; // 1 hr
    public static final int TARGET_SPACING = 4 * 60 + 20; // 4 minute 20 sec
    public static final int INTERVAL = 1; // Every block
    
    /**
     * Blocks with a timestamp after this should enforce BIP 16, aka "Pay to script hash". This BIP changed the
     * network rules in a soft-forking manner, that is, blocks that don't follow the rules are accepted but not
     * mined upon and thus will be quickly re-orged out as long as the majority are enforcing the rule.
     */
    public static final int BIP16_ENFORCE_TIME = 1333238400; // TU ??

    public static final long MAX_COINS = 90000000L;
    public static final Coin MAX_MONEY = COIN.multiply(MAX_COINS);

    /** Alias for MainNetParams.get(), use that instead */
    @Deprecated
    public static NetworkParameters prodNet() {
        return MainNetParams.get();
    }

    /** Returns a testnet params modified to allow any difficulty target. */
    @Deprecated
    public static NetworkParameters unitTests() {
        return UnitTestParams.get();
    }

    /**
     * A Java package style string acting as unique ID for these parameters
     */
    public String getId() {
        return id;
    }

    public abstract String getPaymentProtocolId();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkParameters other = (NetworkParameters) o;
        return getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /** Returns the network parameters for the given string ID or NULL if not recognized. */
    @Nullable
    public static NetworkParameters fromID(String id) {
        if (id.equals(ID_MAINNET)) {
            return MainNetParams.get();
        } else if (id.equals(ID_UNITTESTNET)) {
            return UnitTestParams.get();
        } else {
            return null;
        }
    }

    /** Returns the network parameters for the given string paymentProtocolID or NULL if not recognized. */
    @Nullable
    public static NetworkParameters fromPmtProtocolID(String pmtProtocolId) {
        if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_MAINNET)) {
            return MainNetParams.get();
        } else {
            return null;
        }
    }

    public int getSpendableCoinbaseDepth() {
        return spendableCoinbaseDepth;
    }

    /**
     * Returns true if the NetworkParameters is for a ShapeShift coin. ie. not NuBits
     */
    public boolean isShapeShift() {
        return false;
    }

    /**
     * Returns true if the block height is either not a checkpoint, or is a checkpoint and the hash matches.
     */
    public boolean passesCheckpoint(int height, Sha256Hash hash) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash == null || checkpointHash.equals(hash);
    }

    /**
     * Returns true if the given height has a recorded checkpoint.
     */
    public boolean isCheckpoint(int height) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash != null;
    }

    /** Returns DNS names that when resolved, give IP addresses of active peers. */
    public String[] getDnsSeeds() {
        return dnsSeeds;
    }

    /**
     * <p>Genesis block for this chain.</p>
     *
     * <p>The first block in every chain is a well known constant shared between all MotaCoin implemenetations. For a
     * block to be valid, it must be eventually possible to work backwards to the genesis block by following the
     * prevBlockHash pointers in the block headers.</p>
     *
     * <p>The genesis blocks for both test and prod networks contain the timestamp of when they were created,
     * and a message in the coinbase transaction. It says, <i>"Matonis 07-AUG-2012 Parallel Currencies And The 
     * Roadmap To Monetary Freedom"</i>.</p>
     */
    public Block getGenesisBlock() {
        return genesisBlock;
    }

    /** Default TCP port on which to connect to nodes. */
    public int getPort() {
        return port;
    }

    /** The header bytes that identify the start of a packet on this network. */
    public long getPacketMagic() {
        return packetMagic;
    }

    /**
     * First byte of a base58 encoded address. See {@link com.gig8.motacoinj.core.Address}. This is the same as acceptableAddressCodes[0] and
     * is the one used for "normal" addresses. Other types of address may be encountered with version codes found in
     * the acceptableAddressCodes array.
     */
    public int getAddressHeader() {
        return addressHeader;
    }

    /**
     * First byte of a base58 encoded P2SH address.  P2SH addresses are defined as part of BIP0013.
     */
    public int getP2SHHeader() {
        return p2shHeader;
    }

    /** First byte of a base58 encoded dumped private key. See {@link com.gig8.motacoinj.core.DumpedPrivateKey}. */
    public int getDumpedPrivateKeyHeader() {
        return dumpedPrivateKeyHeader;
    }

    /**
     * How much time in seconds is supposed to pass between "interval" blocks. If the actual elapsed time is
     * significantly different from this value, the network difficulty formula will produce a different value. Both
     * test and production MotaCoin networks use 2 weeks (1209600 seconds).
     */
    public int getTargetTimespan() {
        return targetTimespan;
    }

    /**
     * The version codes that prefix addresses which are acceptable on this network. Although Satoshi intended these to
     * be used for "versioning", in fact they are today used to discriminate what kind of data is contained in the
     * address and to prevent accidentally sending coins across chains which would destroy them.
     */
    public int[] getAcceptableAddressCodes() {
        return acceptableAddressCodes;
    }

    /**
     * If we are running in testnet-in-a-box mode, we allow connections to nodes with 0 non-genesis blocks.
     */
    public boolean allowEmptyPeerChain() {
        return true;
    }

    /** How many blocks pass between difficulty adjustment periods. MotaCoin standardises this to be 2015. */
    public int getInterval() {
        return interval;
    }

    /** Maximum target represents the easiest allowable proof of work. */
    public BigInteger getMaxTarget() {
        return maxTarget;
    }

    /**
     * The key used to sign {@link com.gig8.motacoinj.core.AlertMessage}s. You can use {@link com.gig8.motacoinj.core.ECKey#verify(byte[], byte[], byte[])} to verify
     * signatures using it.
     */
    public byte[] getAlertSigningKey() {
        return alertSigningKey;
    }

    /**
     * Used to parse a coin string into a Monetary for this network.
     */
    public Monetary parseCoin(String str) {
        return Coin.parseCoin(str);
    }

}

// Copyright (c) 2020-2021 Project Wazn
// Copyright (c) 2021 Scala
//
// Please see the included LICENSE file for more information.

package io.wazn.androidminer.api;

import java.util.ArrayList;
import java.util.List;

public final class ProviderData {
    public static class Network {
        public String lastBlockHeight = "";
        public String difficulty = "";
        public String lastRewardAmount = "";
        public String lastBlockTime = "";
        public String hashrate = "";
    }

    public static class Pool {
        public String lastBlockHeight = "";
        public String lastRewardAmount = "";
        public String lastBlockTime = "";
        public String hashrate = "";
        public String blocks = "";
        public String minPayout = "";
        public String miners = "";
        int type = -1;
    }

    public static class Miner {
        public String hashrate = "";
        public String balance = "";
        public String paid = "";
        public String lastShare = "";
        public String shares = "";
        public final List<Payment> payments = new ArrayList<>();
    }

    public static class Coin {
        public String name = "Wazn";
        public String symbol = "WAZN";
        public long units;
        public long denominationUnit = 100L;
    }

    public static class Payment {
        public float amount;
        public float fee;
        public String hash;
        public String timestamp;
    }

    final public Network network = new Network();
    final public Pool pool = new Pool();
    final public Coin coin = new Coin();
    public Miner miner = new Miner();

    public boolean isNew = true;
}

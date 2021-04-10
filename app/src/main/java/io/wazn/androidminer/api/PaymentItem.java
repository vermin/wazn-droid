// Copyright (c) 2019, Mine2Gether.com
//
// Please see the included LICENSE file for more information.
//
// Copyright (c) 2020-2021 Project Wazn
// Copyright (c) 2021 Scala
//
// Please see the included LICENSE file for more information.

package io.wazn.androidminer.api;

import java.util.Comparator;
import java.util.Date;

public class PaymentItem {

    public String mHash, mAmount, mFee;
    public Date mTimestamp;

    public PaymentItem() {
    }

    static public final Comparator<PaymentItem> PaymentComparator = new Comparator<PaymentItem>() {
        @Override
        public int compare(PaymentItem o1, PaymentItem o2) {
            return o1.mTimestamp.compareTo(o2.mTimestamp);
        }
    };
}

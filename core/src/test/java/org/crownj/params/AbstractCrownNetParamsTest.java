/*
 * Copyright by the original author or authors.
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

package org.crownj.params;

import org.crownj.core.Block;
import org.crownj.core.Coin;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbstractcrownNetParamsTest {
    private final AbstractcrownNetParams crown_PARAMS = new AbstractcrownNetParams() {
        @Override
        public Block getGenesisBlock() {
            return null;
        }

        @Override
        public String getPaymentProtocolId() {
            return null;
        }
    };

    @Test
    public void isDifficultyTransitionPoint() {
        assertFalse(crown_PARAMS.isDifficultyTransitionPoint(2014));
        assertTrue(crown_PARAMS.isDifficultyTransitionPoint(2015));
        assertFalse(crown_PARAMS.isDifficultyTransitionPoint(2016));
    }

    @Test
    public void isRewardHalvingPoint() {
        assertTrue(crown_PARAMS.isRewardHalvingPoint(209999));

        assertTrue(crown_PARAMS.isRewardHalvingPoint(419999));

        assertFalse(crown_PARAMS.isRewardHalvingPoint(629998));
        assertTrue(crown_PARAMS.isRewardHalvingPoint(629999));
        assertFalse(crown_PARAMS.isRewardHalvingPoint(630000));

        assertTrue(crown_PARAMS.isRewardHalvingPoint(839999));
    }

    @Test
    public void getBlockInflation() {
        assertEquals(Coin.FIFTY_COINS, crown_PARAMS.getBlockInflation(209998));
        assertEquals(Coin.FIFTY_COINS, crown_PARAMS.getBlockInflation(209999));
        assertEquals(Coin.FIFTY_COINS.div(2), crown_PARAMS.getBlockInflation(210000));
        assertEquals(Coin.FIFTY_COINS.div(2), crown_PARAMS.getBlockInflation(210001));
    }
}

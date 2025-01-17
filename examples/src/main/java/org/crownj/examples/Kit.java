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

package org.crownj.examples;

import org.crownj.core.*;
import org.crownj.kits.WalletAppKit;
import org.crownj.params.TestNet3Params;
import org.crownj.script.Script;
import org.crownj.wallet.Wallet;
import org.crownj.wallet.listeners.KeyChainEventListener;
import org.crownj.wallet.listeners.ScriptsChangeEventListener;
import org.crownj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.crownj.wallet.listeners.WalletCoinsSentEventListener;

import java.io.File;
import java.util.List;

import org.crownj.core.listeners.TransactionConfidenceEventListener;

/**
 * The following example shows how to use the by crownj provided WalletAppKit.
 * The WalletAppKit class wraps the boilerplate (Peers, BlockChain, BlockStorage, Wallet) needed to set up a new SPV crownj app.
 * 
 * In this example we also define a WalletEventListener class with implementors that are called when the wallet changes (for example sending/receiving money)
 */
public class Kit {

    public static void main(String[] args) {

        // First we configure the network we want to use.
        // The available options are:
        // - MainNetParams
        // - TestNet3Params
        // - RegTestParams
        // While developing your application you probably want to use the Regtest mode and run your local crown network. Run crownd with the -regtest flag
        // To test you app with a real network you can use the testnet. The testnet is an alternative crown network that follows the same rules as main network.
        // Coins are worth nothing and you can get coins from a faucet.
        // 
        // For more information have a look at: https://crownj.github.io/testing and https://crown.org/en/developer-examples#testing-applications
        NetworkParameters params = TestNet3Params.get();

        // Now we initialize a new WalletAppKit. The kit handles all the boilerplate for us and is the easiest way to get everything up and running.
        // Have a look at the WalletAppKit documentation and its source to understand what's happening behind the scenes: https://github.com/crownj/crownj/blob/master/core/src/main/java/org/crownj/kits/WalletAppKit.java
        WalletAppKit kit = new WalletAppKit(params, new File("."), "walletappkit-example");

        // In case you want to connect with your local crownd tell the kit to connect to localhost.
        // You must do that in reg test mode.
        //kit.connectToLocalHost();

        // Now we start the kit and sync the blockchain.
        // crownj is working a lot with the Google Guava libraries. The WalletAppKit extends the AbstractIdleService. Have a look at the introduction to Guava services: https://github.com/google/guava/wiki/ServiceExplained
        kit.startAsync();
        kit.awaitRunning();

        kit.wallet().addCoinsReceivedEventListener((wallet, tx, prevBalance, newBalance) -> {
            System.out.println("-----> coins resceived: " + tx.getTxId());
            System.out.println("received: " + tx.getValue(wallet));
        });

        kit.wallet().addCoinsSentEventListener((wallet, tx, prevBalance, newBalance) -> System.out.println("coins sent"));

        kit.wallet().addKeyChainEventListener(keys -> System.out.println("new key added"));

        kit.wallet().addScriptsChangeEventListener((wallet, scripts, isAddingScripts) -> System.out.println("new script added"));

        kit.wallet().addTransactionConfidenceEventListener((wallet, tx) -> {
            System.out.println("-----> confidence changed: " + tx.getTxId());
            TransactionConfidence confidence = tx.getConfidence();
            System.out.println("new block depth: " + confidence.getDepthInBlocks());
        });

        // Ready to run. The kit syncs the blockchain and our wallet event listener gets notified when something happens.
        // To test everything we create and print a fresh receiving address. Send some coins to that address and see if everything works.
        System.out.println("send money to: " + kit.wallet().freshReceiveAddress().toString());

        // Make sure to properly shut down all the running services when you manually want to stop the kit. The WalletAppKit registers a runtime ShutdownHook so we actually do not need to worry about that when our application is stopping.
        //System.out.println("shutting down again");
        //kit.stopAsync();
        //kit.awaitTerminated();
    }

}

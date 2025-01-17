/*
 * Copyright 2014 The crownj team
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

package org.crownj.tools;

import org.crownj.crypto.TrustStoreLoader;
import org.crownj.protocols.payments.PaymentProtocol;
import org.crownj.protocols.payments.PaymentProtocolException;
import org.crownj.protocols.payments.PaymentSession;
import org.crownj.uri.crownURI;
import org.crownj.uri.crownURIParseException;
import org.crown.protocols.payments.Protos;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;

/** Takes a URL or crown URI and prints information about the payment request. */
public class PaymentProtocolTool {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Provide a crown URI or URL as the argument.");
            return;
        }
        dump(args[0]);
    }

    private static void dump(String arg) {
        try {
            URI uri = new URI(arg);
            PaymentSession session;
            if (arg.startsWith("/")) {
                FileInputStream stream = new FileInputStream(arg);
                Protos.PaymentRequest request = Protos.PaymentRequest.parseFrom(stream);
                stream.close();
                session = new PaymentSession(request);
            } else if ("http".equals(uri.getScheme())) {
                session = PaymentSession.createFromUrl(arg).get();
            } else if ("crown".equals(uri.getScheme())) {
                crownURI bcuri = new crownURI(arg);
                final String paymentRequestUrl = bcuri.getPaymentRequestUrl();
                if (paymentRequestUrl == null) {
                    System.err.println("No r= param in crown URI");
                    return;
                }
                session = PaymentSession.createFromcrownUri(bcuri).get();
            } else {
                System.err.println("Unknown URI scheme: " + uri.getScheme());
                return;
            }
            final int version = session.getPaymentRequest().getPaymentDetailsVersion();
            StringBuilder output = new StringBuilder(
                    format("crown payment request, version %d%nDate: %s%n", version, session.getDate()));
            PaymentProtocol.PkiVerificationData pki = PaymentProtocol.verifyPaymentRequestPki(
                    session.getPaymentRequest(), new TrustStoreLoader.DefaultTrustStoreLoader().getKeyStore());
            if (pki != null) {
                output.append(format("Signed by: %s%nIdentity verified by: %s%n", pki.displayName, pki.rootAuthorityName));
            }
            if (session.getPaymentDetails().hasExpires()) {
                output.append(format("Expires: %s%n", new Date(session.getPaymentDetails().getExpires() * 1000)));
            }
            if (session.getMemo() != null) {
                output.append(format("Memo: %s%n", session.getMemo()));
            }
            output.append(format("%n%n%s%n%s", session.getPaymentRequest(), session.getPaymentDetails()));
            System.out.println(output);
        } catch (URISyntaxException | crownURIParseException e) {
            System.err.println("Could not parse URI: " + e.getMessage());
        } catch (PaymentProtocolException.PkiVerificationException e) {
            System.err.println(e.getMessage());
            if (e.certificates != null) {
                for (X509Certificate certificate : e.certificates) {
                    System.err.println("  " + certificate);
                }
            }
        } catch (PaymentProtocolException e) {
            System.err.println("Could not handle payment request: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Interrupted whilst processing/downloading.");
        } catch (ExecutionException e) {
            System.err.println("Failed whilst retrieving payment URL: " + e.getMessage());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IOException | KeyStoreException e) {
            e.printStackTrace();
        }
    }
}

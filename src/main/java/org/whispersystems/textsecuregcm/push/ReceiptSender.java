package org.whispersystems.textsecuregcm.push;

import com.google.common.base.Optional;
import org.whispersystems.textsecuregcm.controllers.NoSuchUserException;
import org.whispersystems.textsecuregcm.entities.MessageProtos.Envelope;
import org.whispersystems.textsecuregcm.federation.FederatedClientManager;
import org.whispersystems.textsecuregcm.federation.NoSuchPeerException;
import org.whispersystems.textsecuregcm.mq.MessageQueueManager;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.AccountsManager;
import org.whispersystems.textsecuregcm.storage.Device;
import org.whispersystems.textsecuregcm.util.Util;

import java.io.IOException;
import java.util.Set;

public class ReceiptSender {

  private final PushSender             pushSender;
  private final FederatedClientManager federatedClientManager;
  private final AccountsManager        accountManager;
  private final MessageQueueManager    messageQueueManager;

  public ReceiptSender(AccountsManager        accountManager,
                       PushSender             pushSender,
                       FederatedClientManager federatedClientManager,
                       MessageQueueManager    messageQueueManager)
  {
    this.federatedClientManager = federatedClientManager;
    this.accountManager         = accountManager;
    this.pushSender             = pushSender;
    this.messageQueueManager    = messageQueueManager;
  }

  public void sendReceipt(Account source, String destination,
                          long messageId, Optional<String> relay)
      throws IOException, NoSuchUserException,
             NotPushRegisteredException, TransientPushFailureException
  {
    String jsonMessage = Util.getJsonMessage(source.getNumber(), destination, messageId,
                                             Envelope.Type.RECEIPT_VALUE, "receipt");
    if (!messageQueueManager.sendMessage(jsonMessage)) {
      throw new IOException("RabbitMQ: Sending error!");
    }

    if (source.getNumber().equals(destination)) {
      return;
    }

    if (relay.isPresent() && !relay.get().isEmpty()) {
      sendRelayedReceipt(source, destination, messageId, relay.get());
    } else {
      sendDirectReceipt(source, destination, messageId);
    }
  }

  public void sendRead(Account source, String destination,
                       long timestamp, Optional<Long> when, Optional<String> relay)
          throws IOException, NoSuchUserException,
          NotPushRegisteredException, TransientPushFailureException
  {
    long whenRead = when.isPresent() ? when.get() : System.currentTimeMillis();
    String jsonMessage = Util.getJsonMessage(source.getNumber(),
                                             destination,
                                             timestamp,
                                             Envelope.Type.READ_VALUE,
                                             "read",
                                             whenRead);
    if (!messageQueueManager.sendMessage(jsonMessage)) {
      throw new IOException("RabbitMQ: Sending error!");
    }

    if (relay.isPresent() && !relay.get().isEmpty()) {
      sendRelayedRead(source, destination, timestamp, relay.get());
    } else {
      sendDirectRead(source, destination, timestamp);
    }
  }

  private void sendRelayedReceipt(Account source, String destination, long messageId, String relay)
      throws NoSuchUserException, IOException
  {
    try {
      federatedClientManager.getClient(relay)
                            .sendDeliveryReceipt(source.getNumber(),
                                                 source.getAuthenticatedDevice().get().getId(),
                                                 destination, messageId);
    } catch (NoSuchPeerException e) {
      throw new NoSuchUserException(e);
    }
  }

  private void sendRelayedRead(Account source, String destination, long messageId, String relay)
          throws NoSuchUserException, IOException
  {
    try {
      federatedClientManager.getClient(relay)
                            .sendDeliveryRead(source.getNumber(),
                                              source.getAuthenticatedDevice().get().getId(),
                                              destination, messageId);
    } catch (NoSuchPeerException e) {
      throw new NoSuchUserException(e);
    }
  }

  private void sendDirectReceipt(Account source, String destination, long messageId)
      throws NotPushRegisteredException, TransientPushFailureException, NoSuchUserException
  {
    Account          destinationAccount = getDestinationAccount(destination);
    Set<Device>      destinationDevices = destinationAccount.getDevices();
    Envelope.Builder message            = Envelope.newBuilder()
                                                  .setSource(source.getNumber())
                                                  .setSourceDevice((int) source.getAuthenticatedDevice().get().getId())
                                                  .setTimestamp(messageId)
                                                  .setType(Envelope.Type.RECEIPT);

    if (source.getRelay().isPresent()) {
      message.setRelay(source.getRelay().get());
    }

    for (Device destinationDevice : destinationDevices) {
      pushSender.sendMessage(destinationAccount, destinationDevice, message.build(), true);
    }
  }

  private void sendDirectRead(Account source, String destination, long messageId)
          throws NotPushRegisteredException, TransientPushFailureException, NoSuchUserException
  {
    Account          destinationAccount = getDestinationAccount(destination);
    Set<Device>      destinationDevices = destinationAccount.getDevices();
    Envelope.Builder message            = Envelope.newBuilder()
                                                  .setSource(source.getNumber())
                                                  .setSourceDevice((int) source.getAuthenticatedDevice().get().getId())
                                                  .setTimestamp(messageId)
                                                  .setDeliveryTimestamp(System.currentTimeMillis())
                                                  .setType(Envelope.Type.READ);

    if (source.getRelay().isPresent()) {
      message.setRelay(source.getRelay().get());
    }

    for (Device destinationDevice : destinationDevices) {
      pushSender.sendMessage(destinationAccount, destinationDevice, message.build(), true);
    }
  }

  private Account getDestinationAccount(String destination)
      throws NoSuchUserException
  {
    Optional<Account> account = accountManager.get(destination);

    if (!account.isPresent()) {
      throw new NoSuchUserException(destination);
    }

    return account.get();
  }

}

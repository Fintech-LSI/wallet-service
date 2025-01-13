package com.fintech.walletservice.service;
import com.fintech.walletservice.dto.requests.TransactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class TransactionProducerService {
  private final KafkaTemplate<String, TransactionRequest> kafkaTemplate;
  @Value("${var.TOPIC.wallet-transaction:transaction-requests}")
  private String TOPIC ;

  public void sendTransactionRequest(TransactionRequest request) {
    Message<TransactionRequest> message = MessageBuilder
      .withPayload(request)
      .setHeader(KafkaHeaders.TOPIC, TOPIC)
      .setHeader("__TypeId__", "transactionRequest")
      .build();
    kafkaTemplate.send(message);
  }
}

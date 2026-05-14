package uz.kapitalbank.pg.payflow.dto.response;

public record StartTransferResponse(
  String processInstanceId,
  Long transferId,
  String status
) {}
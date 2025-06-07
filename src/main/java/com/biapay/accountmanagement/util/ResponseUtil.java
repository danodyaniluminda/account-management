package com.biapay.accountmanagement.util;

import com.biapay.accountmanagement.dto.response.ResponseCode;
import com.biapay.accountmanagement.dto.response.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static ResponseEntity<?> response(ResponseDto<Object> responseDto) {
        if (responseDto.getCode() == ResponseCode.SUCCESS.getCode()) {
            return ResponseEntity.ok().body(responseDto);
        }
        if (responseDto.getCode() == ResponseCode.CREATED.getCode()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        }
        if (responseDto.getCode() == ResponseCode.ACCEPTED.getCode()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDto);
        }
        if (responseDto.getCode() == ResponseCode.NO_CONTENT.getCode()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(responseDto);
        }

        if (responseDto.getCode() == ResponseCode.BAD_REQUEST.getCode()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        }
        if (responseDto.getCode() == ResponseCode.UNAUTHORIZED.getCode()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
        }
        if (responseDto.getCode() == ResponseCode.FORBIDDEN.getCode()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseDto);
        }
        if (responseDto.getCode() == ResponseCode.NOT_FOUND.getCode()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
        }
        if (responseDto.getCode() == ResponseCode.CONFLICT.getCode()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseDto);
        }

        if (responseDto.getCode() == ResponseCode.INTERNAL_ERROR.getCode()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }

        return ResponseEntity.badRequest().body(responseDto);
    }
}

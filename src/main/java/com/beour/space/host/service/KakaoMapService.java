package com.beour.space.host.service;

import com.beour.global.exception.error.errorcode.KakaoMapErrorCode;
import com.beour.global.exception.exceptionType.KakaoMapException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoMapService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    public double[] getLatitudeAndLongitude(String address) {
        validateAddress(address);
        Coordinate coordinate = getCoordinatesByAddress(address);
        return new double[]{coordinate.getLatitude(), coordinate.getLongitude()};
    }

    public Coordinate getCoordinatesByAddress(String address) {
        validateAddress(address);

        String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + address;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("카카오맵 API 호출 실패: 상태코드 {}, 주소: {}", response.getStatusCode(), address);
                throw new KakaoMapException(KakaoMapErrorCode.API_CALL_FAILED);
            }

            return parseCoordinatesFromResponse(response.getBody(), address);

        } catch (Exception e) {
            if (e instanceof KakaoMapException) {
                throw e;
            }
            log.error("카카오맵 API 호출 중 예외 발생: 주소 {}", address, e);
            throw new KakaoMapException(KakaoMapErrorCode.API_CALL_FAILED);
        }
    }

    private void validateAddress(String address) {
        if (!StringUtils.hasText(address)) {
            throw new KakaoMapException(KakaoMapErrorCode.INVALID_ADDRESS_FORMAT);
        }
    }

    private Coordinate parseCoordinatesFromResponse(String responseBody, String address) {
        try {
            JSONObject json = new JSONObject(responseBody);
            JSONArray documents = json.getJSONArray("documents");

            if (documents.isEmpty()) {
                log.warn("주소로 좌표를 찾을 수 없음: {}", address);
                throw new KakaoMapException(KakaoMapErrorCode.ADDRESS_NOT_FOUND);
            }

            JSONObject location = documents.getJSONObject(0);
            double latitude = location.getDouble("y");
            double longitude = location.getDouble("x");

            log.debug("주소 좌표 변환 성공: {} -> 위도: {}, 경도: {}", address, latitude, longitude);
            return new Coordinate(latitude, longitude);

        } catch (Exception e) {
            if (e instanceof KakaoMapException) {
                throw e;
            }
            log.error("카카오맵 API 응답 파싱 실패: 주소 {}", address, e);
            throw new KakaoMapException(KakaoMapErrorCode.API_CALL_FAILED);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Coordinate {
        private double latitude;
        private double longitude;
    }
}

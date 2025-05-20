package com.beour.space.host.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    public double[] getLatLng(String address) {
        Coordinate coordinate = getCoordinatesByAddress(address);
        return new double[]{coordinate.getLatitude(), coordinate.getLongitude()};
    }

    public Coordinate getCoordinatesByAddress(String address) {
        String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + address;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("카카오맵 API 호출 실패: " + response.getStatusCode());
        }

        JSONObject json = new JSONObject(response.getBody());
        JSONArray documents = json.getJSONArray("documents");

        if (documents.isEmpty()) {
            throw new RuntimeException("주소로 좌표를 찾을 수 없습니다: " + address);
        }

        JSONObject location = documents.getJSONObject(0);
        double latitude = location.getDouble("y");
        double longitude = location.getDouble("x");

        return new Coordinate(latitude, longitude);
    }

    @Getter
    @AllArgsConstructor
    public static class Coordinate {
        private double latitude;
        private double longitude;
    }
}

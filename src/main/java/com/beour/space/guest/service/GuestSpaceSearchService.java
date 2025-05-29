package com.beour.space.guest.service;

import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GuestSpaceSearchService {

    private final SpaceRepository spaceRepository;

    public List<Space> search(String keyword){
        List<Space> result = spaceRepository.searchByKeyword("%" + keyword + "%");
        if(result.isEmpty()){
            throw new SpaceNotFoundException("키워드에 해당되는 공간이 없습니다.");
        }

        return result;
    }

}

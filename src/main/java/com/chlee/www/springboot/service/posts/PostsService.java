package com.chlee.www.springboot.service.posts;

import com.chlee.www.springboot.domain.posts.Posts;
import com.chlee.www.springboot.domain.posts.PostsRepository;
import com.chlee.www.springboot.web.dto.PostsResponseDto;
import com.chlee.www.springboot.web.dto.PostsSaveRequestDto;
import com.chlee.www.springboot.web.dto.PostsUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class PostsService {
    private final PostsRepository postsRepository;

    @Transactional
    public Long save(PostsSaveRequestDto requestDto) {
        return postsRepository.save(requestDto.toEntity()).getId();
    }

    @Transactional
    public Long update(Long id, PostsUpdateRequestDto requestDto) { //1
        Posts posts = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + id));
        posts.update(requestDto.getTitle(), requestDto.getContent());
        //근데.. 데이터베이스에 쿼리를 날리는 부분이 없다?
        //PostsRepository를 이용해서 DB도 업데이트 해야하는거 아닌가?
        return id;
    }

    public PostsResponseDto findById (Long id) {
        Posts entity = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + id));

        return new PostsResponseDto(entity);
    }
}

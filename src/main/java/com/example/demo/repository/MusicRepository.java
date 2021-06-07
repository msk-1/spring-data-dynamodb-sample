package com.example.demo.repository;

import com.example.demo.entity.Music;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan
public interface MusicRepository extends CrudRepository<Music, String> {
    List<Music> findByArtist(String artist);

    void deleteByArtist(String artist);
}
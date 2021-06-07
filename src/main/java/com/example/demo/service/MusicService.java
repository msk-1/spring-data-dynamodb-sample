package com.example.demo.service;

import com.example.demo.entity.Music;
import com.example.demo.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MusicService {

	@Autowired
	private MusicRepository repository;

	public Music save(String artist, String songTitle) {
		Music m = new Music(artist, songTitle);
		repository.save(m);
		return m;
	}

	public void deleteByArtist(String artist) {
		repository.deleteByArtist(artist);
	}

	public List<Music> findByArtist(String artist){
		return repository.findByArtist(artist);
	}
}
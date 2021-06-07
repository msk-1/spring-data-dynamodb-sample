package com.example.demo;

import com.example.demo.entity.Music;
import com.example.demo.service.MusicService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MusicController {
	@Autowired
	MusicService musicService;

	Log log = LogFactory.getLog(MusicController.class);

	@GetMapping("/create")
	public Music insertMusic(@RequestParam(value = "artist") String artist, @RequestParam(value = "songTitle") String songTitle) {
		Music m = musicService.save(artist, songTitle);
		log.info("complete insert. artist : " + m.getArtist() + " song title : " + m.getSongTitle());
		return m;
	}

	@GetMapping("/find-artist")
	public List<Music> findByArtist(@RequestParam(value = "artist") String artist) {
		List<Music> list = musicService.findByArtist(artist);
		for (Music m : list) {
			log.info("artist : " + m.getArtist() + " song title : " + m.getSongTitle());
		}
		return list;
	}

	@GetMapping("/delete-music")
	public void deleteByArtist(@RequestParam(value = "artist") String artist) {
		musicService.deleteByArtist(artist);
		log.info("complete delete." + artist);
	}

}
// src/api/mangadex.js
import axios from 'axios';

const API = axios.create({
  baseURL: 'https://api.mangadex.org',
});

export const searchManga = title => API.get('/manga', { params: { title } });

export const getChapters = mangaId =>
  API.get('/chapter', {
    params: {
      manga: mangaId,
      translatedLanguage: ['en'],
      order: { chapter: 'asc' },
    },
  });

export const getChapterPages = chapterId =>
  API.get(`/at-home/server/${chapterId}`);

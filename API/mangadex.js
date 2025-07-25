// src/api/mangadex.js
import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';

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

const creds = {
  grant_type: 'password',
  username: 'kiuen123',
  password: 'Kien121999',
  client_id: 'personal-client-39c56d18-cf28-419e-a317-7b807a371a0a-26e7bf13',
  client_secret: 'Bao92gQPEGkGskfRVMS4lzV87NA8danR',
};

export const login = async () => {
  try {
    const resp = await axios({
      method: 'post',
      url: `https://auth.mangadex.org/realms/mangadex/protocol/openid-connect/token`,
      data: creds,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });
    await AsyncStorage.setItem('accessToken', resp.data.access_token);
    await AsyncStorage.setItem('refreshToken', resp.data.refresh_token);
    return resp.data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};

export const getAllTags = async () => {
  try {
    const resp = await axios({
      method: 'get',
      url: `https://api.mangadex.org/manga/tag`,
      headers: {
        'Content-Type': 'application/json',
      },
    });
    return resp;
  } catch (error) {
    console.error(error);
    throw error;
  }
};
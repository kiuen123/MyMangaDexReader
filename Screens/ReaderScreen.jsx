// screens/ReaderScreen.js
import { useEffect, useState } from 'react';
import { ScrollView, Image, StyleSheet } from 'react-native';
import { getChapterPages } from '../API/mangadex';

export default function ReaderScreen({ route }) {
  const { chapterId } = route.params;
  const [images, setImages] = useState([]);

  useEffect(() => {
    const loadPages = async () => {
      const res = await getChapterPages(chapterId);
      const { baseUrl, chapter } = res.data;
      const pages = chapter.data.map(
        img => `${baseUrl}/data/${chapter.hash}/${img}`,
      );
      setImages(pages);
    };
    loadPages();
  }, [chapterId]);

  return (
    <ScrollView>
      {images.map((url, index) => (
        <Image
          key={index}
          source={{ uri: url }}
          style={styles.image}
          resizeMode="contain"
        />
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  image: {
    width: '100%',
    height: 500,
  },
});

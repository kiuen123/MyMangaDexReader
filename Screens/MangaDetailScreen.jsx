import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  ActivityIndicator,
  StyleSheet,
} from 'react-native';
import { getChapters } from '../API/mangadex';

const MangaDetailScreen = ({ route, navigation }) => {
  const { id } = route.params;
  const [chapters, setChapters] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchChapters = async () => {
      try {
        const res = await getChapters(id);
        setChapters(res.data.data);
      } catch (err) {
        console.error('Error fetching chapters:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchChapters();
  }, [id]);

  const renderItem = ({ item }) => {
    const chapNum = item.attributes.chapter || 'N/A';
    const lang = item.attributes.translatedLanguage?.toUpperCase() || '??';

    return (
      <TouchableOpacity
        style={styles.chapterItem}
        onPress={() => navigation.navigate('Reader', { chapterId: item.id })}
      >
        <Text style={styles.chapterText}>
          📖 Chapter {chapNum} [{lang}]
        </Text>
      </TouchableOpacity>
    );
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Danh sách chương</Text>
      {loading ? (
        <ActivityIndicator size="large" />
      ) : (
        <FlatList
          data={chapters}
          keyExtractor={item => item.id}
          renderItem={renderItem}
          contentContainerStyle={styles.flatListContent}
        />
      )}
    </View>
  );
};

export default MangaDetailScreen;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    backgroundColor: '#fff',
  },
  title: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 12,
  },
  chapterItem: {
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#ddd',
  },
  chapterText: {
    fontSize: 16,
  },
  flatListContent: {
    paddingBottom: 100,
  },
});

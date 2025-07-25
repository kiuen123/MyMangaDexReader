// show all tags
import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
} from 'react-native';
import { getAllTags } from '../API/mangadex';
export default function TagListScreen({ navigation }) {
  const [tags, setTags] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTags = async () => {
      try {
        const res = await getAllTags();
        console.log('res', res);
        
        setTags(res.data.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchTags();
  }, []);

  const renderItem = ({ item }) => (
    <TouchableOpacity
      style={styles.tagItem}
      onPress={() => navigation.navigate('MangaList', { tag: item.id })}
    >
      <Text style={styles.tagText}>{item.attributes.name.en}</Text>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Danh sách thẻ</Text>
      {loading ? (
        <Text>Loading...</Text>
      ) : (
        <FlatList
          data={tags}
          keyExtractor={item => item.id}
          renderItem={renderItem}
          contentContainerStyle={styles.flatListContent}
        />
      )}
    </View>
  );
}
const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 10,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  tagItem: {
    padding: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#ccc',
  },
  tagText: {
    fontSize: 18,
  },
  flatListContent: {
    paddingBottom: 20,
  },
});

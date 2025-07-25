// screens/HomeScreen.js
import { useState } from 'react';
import {
  View,
  TextInput,
  Button,
  FlatList,
  Text,
  TouchableOpacity,
  StyleSheet,
} from 'react-native';
import { searchManga } from '../API/mangadex';

export default function HomeScreen({ navigation }) {
  const [keyword, setKeyword] = useState('');
  const [results, setResults] = useState([]);

  const onSearch = async () => {
    const res = await searchManga(keyword);
    setResults(res.data.data);
  };

  return (
    <View style={styles.container}>
      <TextInput
        value={keyword}
        onChangeText={setKeyword}
        placeholder="Search Manga"
        style={styles.input}
      />
      <Button title="Search" onPress={onSearch} />
      <FlatList
        data={results}
        keyExtractor={item => item.id}
        renderItem={({ item }) => (
          <TouchableOpacity
            onPress={() => navigation.navigate('MangaDetail', { id: item.id })}
            style={styles.item}
          >
            <Text>{item.attributes.title.en}</Text>
          </TouchableOpacity>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 10,
  },
});

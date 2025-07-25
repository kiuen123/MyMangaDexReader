// tự động đăng nhập
import React, { useEffect, useState } from 'react';
import { View, Text, Button, StyleSheet, ToastAndroid } from 'react-native';
import { login } from '../API/mangadex';
export default function LoginScreen({ navigation }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const autoLogin = async () => {
      setLoading(true);
      try {
        const data = await login();
        // thông báo đăng nhập thành công
        ToastAndroid.show('Login successful!', ToastAndroid.SHORT);
        // Navigate to MainScreen after successful login
        navigation.navigate('Main');
      } catch (err) {
        setError('Login failed. Please try again.');
        // thông báo lỗi đăng nhập
        ToastAndroid.show('Login failed. Please try again.', ToastAndroid.SHORT);
      } finally {
        setLoading(false);
      }
    };

    autoLogin();
  }, [navigation]);

  return (
    <View style={styles.container}>
      {loading ? (
        <Text>Loading...</Text>
      ) : (
        <>
          {error && <Text style={styles.error}>{error}</Text>}
        </>
      )}
    </View>
  );
}
const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  error: {
    color: 'red',
    marginBottom: 20,
  },
});
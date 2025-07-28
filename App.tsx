// App.js
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import LoginScreen from './Screens/LoginScreen';
import MainScreen from './Screens/MainScreen';
import MangaDetailScreen from './Screens/MangaDetailScreen';
import ReaderScreen from './Screens/ReaderScreen';

const Stack = createNativeStackNavigator();

export default function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName="Login">
        <Stack.Screen name="Login" component={LoginScreen} options={{ headerShown: false }} />
        <Stack.Screen name="Main" component={MainScreen} options={{ headerShown: false }} />
        <Stack.Screen name="MangaDetail" component={MangaDetailScreen} options={{ headerShown: false }} />
        <Stack.Screen name="Reader" component={ReaderScreen} options={{ headerShown: false }} />
      </Stack.Navigator>
    </NavigationContainer>
  );  
}

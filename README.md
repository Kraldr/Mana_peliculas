# Mana Peliculas

# Descripción

Esta aplicación te permite explorar y descubrir tus películas y series favoritas de una manera fácil y divertida. Aquí hay una descripción de algunas de las tecnologías y patrones que utilizamos para crear esta aplicación:

#Firebase

Firebase es una plataforma de desarrollo de aplicaciones en la nube que utilizamos para una variedad de funciones, incluyendo:

- Firebase Realtime Database: Almacenamos datos importantes, como listas de reproducción y favoritos de usuarios, en la base de datos en tiempo real de Firebase.
- Autenticación de Firebase: Permitimos que los usuarios creen y gestionen sus perfiles con Firebase Authentication para que puedan guardar sus preferencias y acceder a sus listas personalizadas.
- Firebase Cloud Messaging: Utilizamos FCM para notificar a los usuarios sobre nuevas películas, series o actualizaciones en sus programas favoritos.

#Glide

Glide es una biblioteca de gestión de imágenes que utilizamos para cargar y mostrar imágenes en nuestra aplicación. Con Glide, podemos:

- Cargar imágenes desde una variedad de fuentes, incluyendo URL remotas y recursos locales.
- Realizar almacenamiento en caché eficiente para mejorar el rendimiento de la carga de imágenes.
- Redimensionar y recortar imágenes para que se ajusten perfectamente a nuestros diseños y pantallas.

#MVVM (Model-View-ViewModel)

Utiliza el patrón de diseño MVVM para separar claramente la lógica de presentación de la lógica de negocio. Aquí hay un resumen de cómo implemente MVVM:

- Model: Representa nuestros datos y reglas de negocio. Esto incluye clases para películas, series, usuarios y más.
- View: Nuestras actividades y fragmentos que representan la interfaz de usuario de la aplicación. Los componentes de la vista no contienen lógica de negocio.
- ViewModel: Actúa como un intermediario entre el modelo y la vista. Contiene la lógica de presentación y se encarga de actualizar la vista cuando cambian los datos.
  
#Otras Tecnologías

OkHttp: Utilizamos OkHttp para realizar solicitudes HTTP y obtener datos de fuentes externas, como sitios web de películas y series.
Jsoup: Jsoup es una biblioteca de análisis HTML que nos ayuda a extraer información relevante de sitios web externos.
Lottie: Integración de Lottie para crear animaciones atractivas que mejoren la experiencia del usuario.

Estas tecnologías y patrones nos permiten crear una aplicación de películas robusta, eficiente y atractiva para nuestros usuarios.

#Capturas de Pantalla

Captura de pantalla 1
Captura de pantalla 2
Captura de pantalla 3

# Mana Películas

# Descripción

Esta aplicación te permite explorar y descubrir tus películas y series favoritas de una manera fácil y divertida. Aquí hay una descripción de algunas de las tecnologías y patrones que utilizamos para crear esta aplicación:

# Firebase

Firebase es una plataforma de desarrollo de aplicaciones en la nube que utilizamos para una variedad de funciones, incluyendo:

- Firebase Realtime Database: Almacenamos datos importantes, como listas de reproducción y favoritos de usuarios, en la base de datos en tiempo real de Firebase.
- Autenticación de Firebase: Permitimos que los usuarios creen y gestionen sus perfiles con Firebase Authentication para que puedan guardar sus preferencias y acceder a sus listas personalizadas.
- Firebase Cloud Messaging: Utilizamos FCM para notificar a los usuarios sobre nuevas películas, series o actualizaciones en sus programas favoritos.

# Glide

Glide es una biblioteca de gestión de imágenes que utilizamos para cargar y mostrar imágenes en nuestra aplicación. Con Glide, podemos:

- Cargar imágenes desde una variedad de fuentes, incluyendo URL remotas y recursos locales.
- Realizar almacenamiento en caché eficiente para mejorar el rendimiento de la carga de imágenes.
- Redimensionar y recortar imágenes para que se ajusten perfectamente a nuestros diseños y pantallas.

# MVVM (Model-View-ViewModel)

Utiliza el patrón de diseño MVVM para separar claramente la lógica de presentación de la lógica de negocio. Aquí hay un resumen de cómo implemente MVVM:

- Model: Representa nuestros datos y reglas de negocio. Esto incluye clases para películas, series, usuarios y más.
- View: Nuestras actividades y fragmentos que representan la interfaz de usuario de la aplicación. Los componentes de la vista no contienen lógica de negocio.
- ViewModel: Actúa como un intermediario entre el modelo y la vista. Contiene la lógica de presentación y se encarga de actualizar la vista cuando cambian los datos.
  
# Otras Tecnologías

- OkHttp: Utilizamos OkHttp para realizar solicitudes HTTP y obtener datos de fuentes externas, como sitios web de películas y series.
- Jsoup: Jsoup es una biblioteca de análisis HTML que nos ayuda a extraer información relevante de sitios web externos.
- Lottie: Integración de Lottie para crear animaciones atractivas que mejoren la experiencia del usuario.
- Coroutines: Hemos implementado el uso de coroutines para realizar operaciones asíncronas de manera eficiente en la aplicación. Esto nos permite mantener la interfaz de usuario receptiva mientras realizamos solicitudes de red y otras tareas que pueden llevar tiempo. Por ejemplo, cuando obtenemos datos de películas recomendadas desde una fuente externa, utilizamos coroutines para realizar esta operación en segundo plano y luego actualizar la interfaz de usuario de manera sincronizada.

Estas tecnologías y patrones nos permiten crear una aplicación de películas robusta, eficiente y atractiva.

# Capturas de Pantalla

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/c2fd1ad8-23af-43db-a186-6563d110403a)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/6f3018db-0e20-40d7-88a5-4d79ae9d96c6)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/c52be355-bafa-4634-a692-1700b98e3670)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/33db7b2c-0855-4a4d-a6df-f19268407ebe)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/f273f05e-7ad5-4047-86e9-ab087df10c53)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/569b5f01-0215-4087-9117-5436aea53489)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/0816d4e2-2773-449a-93ad-960bef475725)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/e4ce9c90-cd06-467c-9fd3-9d2d486c624d)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/e7687400-f0b2-4200-b9bd-3f80878f35c4)













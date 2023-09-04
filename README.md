# Mana Peliculas

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

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/01269023-7952-4795-b566-920a8d6455b3)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/01e58da3-655b-41c9-82c8-73ed4936a2e3)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/0144d400-eeb6-46de-b585-213f3d6be800)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/0649527e-2dad-4b97-9509-fb84890d80f1)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/e0663275-7ede-40e6-bf22-b3a5c79a02f4)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/61b030a3-e8e9-49f8-9614-cb714e43220e)

![image](https://github.com/Kraldr/Mana_peliculas/assets/44440933/814bed64-1a79-4bc3-a196-6d01f9364537)







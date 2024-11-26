# ErasmusNest

<p align="center">
  <img src="assets/logo.png" alt="ErasmusNest Logo" width="100">
</p>

ErasmusNest is an application designed to help Erasmus students find accommodation while allowing property owners to advertise their listings. The app features a review system and personalized recommendations based on users' preferences and "likes." Its primary strength lies in its high scalability, achieved through the use of the following technologies:

- **Redis**: For caching user logins and reservations, improving performance.
- **Neo4J**: For managing relationships between users and apartments, leveraging graph database capabilities.
- **MongoDB**: For storing apartment listings and user data, ensuring flexibility and scalability.

The project was developed as part of the **Large Scale and Multistructured Databases** course @Unipi, in A.A. 2023/2024.

<p align="center">
  <img src="assets/apartment_view.png" alt="Apartment view" width="800">
</p>

Extensive information on the project can be found in the [final report](ErasmusNest_Report_LargeScale.pdf).

## Features

- **Apartment Search**: Students can search for apartments using various filters such as city, price, number of bathrooms, and more. Detailed listings are available for each apartment.
- **Reviews**: Users can leave reviews for apartments they have stayed in, including ratings and comments. They can also view reviews from other users.
- **Apartment Management**: Property owners can upload new listings, update details of existing properties, and remove listings when necessary.
- **Personalized Recommendations**: Based on user preferences and "likes," the app provides tailored apartment suggestions to enhance the user experience.
- **Booking Management**: Users can view and manage their active bookings, including details like dates, price, and booking status.
- **Profile Management**: Users can update their personal information, such as field of study and city of interest, to receive more relevant apartment recommendations.

## License

This project is distributed under the **Apache License**. See the [LICENSE](LICENSE) file for more details.

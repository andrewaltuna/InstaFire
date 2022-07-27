# InstaFire
An app inspired by Instagram that uses Firebase API as its backend!

## Features
- Allow users to create posts and view posts other users have submitted through your feed
- User authentication created using Firebase Authentication
  - Sign up and create your profile within the app
- View your or other users' profiles and see their posts
- Store post and image data within Firebase Cloud Storage and Firestore

## Demo

### Login and Signup
This function allows a user to gain access to their account if created previously. Users who do not have an account can create one, starting with inputting the login credentials they wish to use, followed by their profile details. Once logged in, users' sessions persist even though the app is closed. If a user wishes to sign out, they must manually do so in their profile page.

![](https://media.giphy.com/media/YzJqC5H1guvmhFFPTx/giphy.gif)

### Feed
This function allows users to see all posts created by other users ordered by time created. By default, the number of entries queried from Firebase Firestore is limited to 20.

![](https://media.giphy.com/media/F3PACodYz5qL5N6DDY/giphy.gif)

### Post Creation
This function allows logged in users to create posts under their account. The user must upload an image and enter a description before being allowed to submit the post.

![](https://media.giphy.com/media/iPjmcaxKS5LNTCzzsG/giphy.gif)

### Profile View
This function allows you to see a user's profile and posts. The user's display image, username, bio, and number of posts are shown. Additionally, the user's posts are displayed in a grid, which once clicked, displays all of the user's posts in a RecyclerView in a similar fashion to the feed.

![](https://media.giphy.com/media/6XtUo8y7kbrZwLcThf/giphy.gif)

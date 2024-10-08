# SplitMate

**SplitMate** is a dynamic and responsive Android application designed to help users manage personal and group expenses effortlessly. It calculates expenses, splits costs among group members, and shows who owes and who is owed. The app ensures a smooth and efficient way to keep track of individual and group spending.

## Features

- **Personal Expenses**: Users can add and track their personal expenses such as food, travel, shopping, etc.
- **Group Expense Management**: Create groups, add members, and track shared expenses in real-time. The app calculates each member's share and shows the final balance.
- **Expense Splitting**: Automatically splits group expenses among members and identifies who owes and who is owed.
- **Firebase Authentication**: Users can securely log in using Firebase Authentication.
- **Firestore Database**: All expenses and groups are stored in real-time using Firestore.
- **Dynamic Calculation**: Automatically calculates who will pays how much amount to whom.



## Installation and Setup

   1. Clone the repository:
   
      ```bash
      git clone https://github.com/vasuzadafiya/SplitMate.git
      ```
      
   2. Open the project in Android Studio.
   
   3. Set up Firebase:
       - Create a Firebase project.
       - Enable Authentication and Firestore Database.
       - Download the `google-services.json` file and place it in the `/app` directory.
   
   4. Run the app on an Android emulator or physical device.

## Database Structure

- **Users (Collection)**:
  - Fields: `email`, `PhoneNumber`, `uid`
  - **PersonalExpenses (Subcollection)**:
    - Fields: `amount`, `date`, `category`, `paymentMethod`
  - **Groups (Subcollection)**:
    - Fields: `groupname`, `createdAt`, `members`, `uid`
    - **Expense (Subcollection)**:
      - Fields: `amount`, `date`, `category`, `paidBy`

## Future Inhancement

- **Notifications**: Notify users of unsettled payments.
- **Export Functionality**: Export expenses to PDF or Excel.
- **Multi-Currency Support**: Handle currency conversion for group expenses.

## Acknowledgments

I would like to thank my friends, family, faculty, and the open-source community for their guidance and support during the development of this project.

i will add documentation very soon...

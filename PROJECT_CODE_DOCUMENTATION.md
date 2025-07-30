# COMP1876 SU25 - Android Gym Management System
## Complete Code Documentation & System Flow Analysis

### 📱 PROJECT OVERVIEW
This Android application is a comprehensive gym management system built using Jetpack Compose and Firebase. The app manages three main entities: Classes, Teachers, and Users with full CRUD operations and real-time data synchronization.

---

## 🏗️ SYSTEM ARCHITECTURE

### 1. APPLICATION ENTRY POINT
**File:** `MainActivity.kt`
```
Purpose: Main activity that serves as the application entry point
Key Components:
- ComponentActivity with edge-to-edge display
- Material3 theming integration
- AuthViewModel dependency injection
- Scaffold layout with navigation integration

System Flow:
1. App launches → MainActivity.onCreate()
2. Enables edge-to-edge display
3. Initializes AuthViewModel
4. Sets up Material3 theme
5. Launches MyAppNavigation with padding
```

### 2. NAVIGATION ARCHITECTURE
**File:** `MyAppNavigation.kt`
```
Purpose: Centralized navigation management with parameter passing
Navigation Routes:
- "intro" → IntroPage (app entry point)
- "login" → LoginPage 
- "signup" → SignupPage
- "home" → HomePage (main dashboard)
- "addclass" → AddClassScreen
- "updateclass/{classId}" → UpdateClassScreen
- "addteacher" → AddTeacherScreen  
- "updateteacher/{teacherId}" → UpdateTeacherScreen
- "updateuser/{userId}" → UpdateUserScreen
- "add_class_detail/{classId}/{courseId}" → AddClassDetailScreen
- "update_class_detail/{classId}/{courseId}/{detailId}" → UpdateClassDetailScreen

System Flow:
1. NavHost manages all screen transitions
2. Deep linking with parameter extraction
3. Type-safe navigation with NavType.StringType
4. Backstack management for proper navigation flow
```

---

## 🔐 AUTHENTICATION SYSTEM

### 3. AUTHENTICATION VIEW MODEL
**File:** `AuthViewModel.kt`
```
Purpose: Manages authentication state and Firebase Auth operations
Key Functions:
- login(email, password): Firebase email/password authentication
- signup(email, password): User registration
- logout(): Sign out current user
- AuthState management (Authenticated, Unauthenticated, Loading, Error)

System Flow:
1. ViewModel observes Firebase Auth state
2. Updates LiveData for UI observation
3. Handles authentication errors
4. Manages loading states during operations
```

### 4. INTRO PAGE
**File:** `pages/IntroPage.kt`
```
Purpose: Welcome screen with authentication state monitoring
Key Features:
- LaunchedEffect for auth state observation
- Automatic navigation on authentication
- Error handling with Toast messages
- "Get Started" button navigation to login

System Flow:
1. User opens app → IntroPage displayed
2. AuthViewModel checks existing auth state
3. If authenticated → navigate to "home"
4. If error → show Toast message
5. User clicks "Get Started" → navigate to "login"
```

### 5. LOGIN PAGE
**File:** `pages/LoginPage.kt`
```
Purpose: User authentication interface
Key Features:
- Email/password input fields
- Form validation
- Firebase authentication integration
- Navigation to signup page
- Error state management

System Flow:
1. User enters credentials
2. Form validation checks
3. AuthViewModel.login() called
4. Firebase Auth processes request
5. On success → navigate to "home"
6. On error → display error message
```

### 6. SIGNUP PAGE
**File:** `pages/SignupPage.kt`
```
Purpose: New user registration interface
Key Features:
- Email/password registration form
- Password confirmation validation
- Firebase user creation
- Automatic login after registration

System Flow:
1. User fills registration form
2. Password confirmation validation
3. AuthViewModel.signup() called
4. Firebase creates new user account
5. On success → navigate to "home"
6. On error → display validation messages
```

---

## 🏠 MAIN DASHBOARD SYSTEM

### 7. HOME PAGE
**File:** `pages/HomePage.kt`
```
Purpose: Main dashboard with bottom tab navigation
Key Features:
- Nested navigation controller for tabs
- Three main sections: Classes, Teachers, Users
- Bottom navigation bar
- Global sign-out functionality
- Authentication state monitoring

Tab Structure:
- "class" tab → ClassPage
- "teacher" tab → TeacherPage  
- "user" tab → UserPage

System Flow:
1. After authentication → HomePage displayed
2. Bottom navigation manages tab switching
3. Nested NavHost handles tab content
4. Sign out button calls AuthViewModel.logout()
5. If unauthenticated → navigate back to "intro"
```

### 8. TAB NAVIGATION SYSTEM
**File:** `tabview/TabView.kt`
```
Purpose: Bottom navigation bar with badge support
Components:
- TabBarItem: Data class for tab configuration
- TabView: Main navigation bar composable
- TabBarIconView: Individual tab icon with badge
- TabBarBadgeView: Badge notification display

Key Features:
- Dynamic icon switching (selected/unselected)
- Badge notification system
- Navigation state persistence
- Material3 NavigationBar implementation

System Flow:
1. TabView receives list of TabBarItem
2. Current route tracked via NavController
3. Selected state determined by route hierarchy
4. Icons switch based on selection state
5. Badge count displayed if provided
```

---

## 🏋️ CLASS MANAGEMENT SYSTEM

### 9. CLASS PAGE (Main CRUD Interface)
**File:** `Site_pages/ClassPage.kt`
```
Purpose: Comprehensive class management with CRUD operations
Key Features:
- Real-time data loading from Firebase
- Search functionality with filtering
- Expandable cards showing class details
- Swipe-to-refresh functionality
- Floating Action Button for adding classes

UI Components:
- TopAppBar with logout functionality
- Search TextField with real-time filtering
- SwipeRefresh wrapper for pull-to-refresh
- LazyColumn for efficient list rendering
- CourseCard for each class item

System Flow:
1. LaunchedEffect triggers data loading
2. classFirebaseRepository.getClassesWithDetails() called
3. Classes list updated in state
4. Search query filters displayed classes
5. User interactions trigger navigation or actions
```

### 10. COURSE CARD COMPONENT
**File:** `Site_pages/ClassPage.kt` (CourseCard composable)
```
Purpose: Individual class display with expandable details
Key Features:
- Animated expansion/collapse
- Class information cards (Day, Time, Duration, Price)
- Dropdown menu for CRUD operations
- Expandable section for class session details
- Teacher name resolution

UI Elements:
- Class type badge
- Quick info cards (Day, Time, Duration, Price, Capacity)
- Description section
- Expandable button with rotation animation
- Class sessions list when expanded

System Flow:
1. Card displays basic class information
2. User clicks dropdown → shows Update/Delete options
3. User clicks expand → loads class session details
4. LaunchedEffect fetches teacher names
5. ClassCard components show individual sessions
```

### 11. CLASS CARD COMPONENT
**File:** `Site_pages/ClassPage.kt` (ClassCard composable)
```
Purpose: Individual class session display within expanded CourseCard
Key Features:
- Date and price display
- Teacher and capacity information
- Clickable to show details dialog
- Material3 card design

System Flow:
1. Displays specific class session data
2. Shows instructor name and capacity
3. Click triggers ClassDetailsDialog
4. Provides quick overview of session details
```

### 12. ADD CLASS SCREEN
**File:** `functionPages/Class/AddClassScreen.kt`
```
Purpose: Form interface for creating new gym classes
Key Features:
- Class type selection dropdown
- Day of week selection
- Time picker integration
- Duration and capacity input
- Price and description fields
- Form validation

System Flow:
1. User fills out class creation form
2. Validation checks required fields
3. classFirebaseRepository.addClass() called
4. Firebase creates new class document
5. Success → navigate back to ClassPage
6. Error → display error message
```

### 13. UPDATE CLASS SCREEN
**File:** `functionPages/Class/UpdateClassScreen.kt`
```
Purpose: Form interface for modifying existing classes
Key Features:
- Pre-populated form fields with existing data
- Same validation as AddClassScreen
- Update operation instead of create
- Data persistence on updates

System Flow:
1. Receives classId parameter from navigation
2. Loads existing class data from Firebase
3. Populates form fields with current values
4. User modifies desired fields
5. classFirebaseRepository.updateClass() called
6. Success → navigate back with updated data
```

---

## 🧑‍🏫 TEACHER MANAGEMENT SYSTEM

### 14. TEACHER PAGE
**File:** `Site_pages/TeacherPage.kt`
```
Purpose: Teacher directory and management interface
Key Features:
- Teacher profile cards with contact information
- Specialization display
- Search and filter capabilities
- FloatingActionButton for adding teachers
- Swipe-to-refresh functionality

UI Components:
- Search TextField for name filtering
- LazyColumn with TeacherCard items
- Empty state handling
- Loading states with refresh indicator

System Flow:
1. teacherFirebaseRepository.getTeachers() loads data
2. Teachers displayed in filterable list
3. Search query filters by teacher name
4. TeacherCard shows individual teacher info
5. Click triggers TeacherDetailsDialog
```

### 15. TEACHER CARD COMPONENT
**File:** `Site_pages/TeacherPage.kt` (TeacherCard composable)
```
Purpose: Individual teacher profile display
Key Features:
- Teacher name and specialization
- Contact information (email, phone)
- Profile picture placeholder
- Click to view details

System Flow:
1. Displays teacher basic information
2. Shows specialization and contact details
3. Click triggers TeacherDetailsDialog
4. Provides quick teacher overview
```

### 16. ADD TEACHER SCREEN
**File:** `functionPages/Teacher/AddTeacherScreen.kt`
```
Purpose: Form for adding new gym instructors
Key Features:
- Personal information input (name, email, phone)
- Specialization selection
- Experience and qualification fields
- Form validation for required fields

System Flow:
1. User fills teacher registration form
2. Validation ensures required fields completed
3. teacherFirebaseRepository.addTeacher() called
4. Firebase creates teacher document
5. Success → navigate back to TeacherPage
```

### 17. UPDATE TEACHER SCREEN
**File:** `functionPages/Teacher/UpdateTeacherScreen.kt`
```
Purpose: Form for modifying teacher information
Key Features:
- Pre-loaded teacher data
- Editable fields for all teacher information
- Update validation
- Data persistence

System Flow:
1. Receives teacherId from navigation
2. Loads current teacher data
3. Populates form with existing information
4. User modifies fields as needed
5. teacherFirebaseRepository.updateTeacher() called
```

### 18. TEACHER DETAILS DIALOG
**File:** `functionPages/Teacher/TeacherDetailsDialog.kt`
```
Purpose: Popup displaying comprehensive teacher information
Key Features:
- Complete teacher profile view
- Contact information display
- Specialization and experience details
- Action buttons for editing
- Material3 dialog design

System Flow:
1. Triggered from TeacherCard click
2. Displays full teacher profile
3. Edit button navigates to UpdateTeacherScreen
4. Close button dismisses dialog
```

---

## 👥 USER MANAGEMENT SYSTEM

### 19. USER PAGE
**File:** `Site_pages/UserPage.kt`
```
Purpose: Member directory and user management
Key Features:
- User profile management
- Contact information display
- User status tracking
- Search and filtering system
- Member statistics

UI Components:
- Search functionality for user names
- UserCard for individual profiles
- Status indicators for active members
- SwipeRefresh for data updates

System Flow:
1. userFirebaseRepository.getUsers() loads members
2. Users displayed in searchable list
3. Filter by name or membership status
4. UserCard shows member information
5. Click triggers UserDetailsDialog
```

### 20. USER CARD COMPONENT
**File:** `Site_pages/UserPage.kt` (UserCard composable)
```
Purpose: Individual user/member profile display
Key Features:
- Member name and status
- Contact information
- Membership type indicator
- Join date display

System Flow:
1. Shows basic member information
2. Displays membership status
3. Click opens UserDetailsDialog
4. Provides member overview
```

### 21. USER DETAILS DIALOG
**File:** `functionPages/User/UserDetailDialog.kt`
```
Purpose: Comprehensive user profile popup
Key Features:
- Complete member profile view
- Contact information display
- Membership status and details
- Edit functionality
- Professional dialog design

UI Components:
- Member badge display
- Detailed contact information
- Action buttons for editing
- Close button functionality

System Flow:
1. Triggered from UserCard click
2. Shows complete user profile
3. Edit button navigates to UpdateUserScreen
4. Displays membership information
5. Close dismisses dialog
```

### 22. UPDATE USER SCREEN
**File:** `functionPages/User/UpdateUserDialog.kt`
```
Purpose: Form for modifying user/member information
Key Features:
- Pre-populated user data
- Contact information editing
- Membership status updates
- Profile picture management

System Flow:
1. Receives userId from navigation
2. Loads current user data
3. Allows editing of user information
4. userFirebaseRepository.updateUser() called
5. Updates member profile data
```

---

## 📅 CLASS DETAILS MANAGEMENT

### 23. CLASS DETAILS DIALOG
**File:** `functionPages/Courses/ClassDetailsDialog.kt`
```
Purpose: Popup showing specific class session information
Key Features:
- Class session details display
- Teacher information
- Date, time, and pricing
- Capacity and enrollment info
- Update navigation button

System Flow:
1. Triggered from ClassCard click
2. Shows specific session details
3. Displays assigned teacher information
4. Update button navigates to UpdateClassDetailScreen
5. Close button dismisses dialog
```

### 24. ADD CLASS DETAIL SCREEN
**File:** `functionPages/Courses/AddClassDetailScreen.kt`
```
Purpose: Form for creating specific class sessions
Key Features:
- Date selection with day-of-week validation
- Teacher assignment dropdown
- Price and capacity management
- Smart date picker (only allows dates matching parent class day)

Date Restriction Logic:
- If parent class is "Monday", only Monday dates selectable
- Prevents scheduling conflicts
- Validates date against class day_of_week

System Flow:
1. Receives classId and courseId parameters
2. Loads parent class information
3. Restricts date selection to matching day
4. Teacher dropdown populated from Firebase
5. Creates new class session document
```

### 25. UPDATE CLASS DETAIL SCREEN
**File:** `functionPages/Courses/UpdateClassDetailScreen.kt`
```
Purpose: Form for modifying existing class sessions
Key Features:
- Pre-loaded session data
- Same date validation as add screen
- Teacher reassignment capability
- Price and capacity updates

System Flow:
1. Receives classId, courseId, and detailId
2. Loads existing class session data
3. Populates form with current values
4. Applies same validation rules
5. Updates existing class session
```

---

## 🗄️ DATA MANAGEMENT LAYER

### 26. CLASS DATA MODEL
**File:** `controllers/dataClasses/classModel.kt`
```
Purpose: Data structure for gym class information
Properties:
- id: String (unique identifier)
- type_of_class: String (e.g., "Yoga", "Cardio")
- day_of_week: String (e.g., "Monday")
- time_of_course: String (class time)
- duration: Int (minutes)
- capacity: Int (max students)
- price_per_class: Double
- description: String
- classes: Map<String, Any> (nested course data)

System Flow:
1. Firebase documents mapped to this data class
2. Used throughout UI for type safety
3. Validation ensures data integrity
4. Serialization for Firebase storage
```

### 27. CLASS DETAILS MODEL
**File:** `controllers/dataClasses/classDetailsModel.kt`
```
Purpose: Data structure for specific class sessions
Properties:
- id: String (session identifier)
- date: String (specific date)
- teacher: String (teacher ID)
- price: Double (session price)
- capacity: Int (session capacity)
- type_of_class: String (inherited from parent)

System Flow:
1. Represents individual class sessions
2. Links to parent class via classId
3. References teacher via teacher ID
4. Used in expandable class details
```

### 28. TEACHER DATA MODEL
**File:** `controllers/dataClasses/teacherModel.kt`
```
Purpose: Data structure for instructor information
Properties:
- id: String (unique identifier)
- name: String (full name)
- email: String (contact email)
- phone: String (contact number)
- specialization: String (expertise area)
- experience: String (years of experience)
- qualifications: String (certifications)

System Flow:
1. Firebase teacher documents mapped to this model
2. Used in teacher management screens
3. Referenced in class session assignments
4. Provides type safety for teacher operations
```

### 29. USER DATA MODEL
**File:** `controllers/dataClasses/userModel.kt`
```
Purpose: Data structure for gym members/users
Properties:
- id: String (unique identifier)
- name: String (member name)
- email: String (contact email)
- phone: String (contact number)
- membershipType: String (membership level)
- joinDate: String (registration date)
- status: String (active/inactive)

System Flow:
1. Firebase user documents mapped to this model
2. Used in user management interfaces
3. Tracks membership information
4. Enables user profile management
```

---

## 🔥 FIREBASE REPOSITORY LAYER

### 30. CLASS FIREBASE REPOSITORY
**File:** `controllers/classFirebaseRepository.kt`
```
Purpose: Firebase operations for class management
Key Functions:
- getClassesWithDetails(): Loads all classes with nested data
- getClassDetailsForCourse(): Loads specific course sessions
- addClass(): Creates new class document
- updateClass(): Updates existing class
- deleteClass(): Removes class document
- addClassDetail(): Creates class session
- updateClassDetail(): Updates class session

System Flow:
1. Provides abstraction over Firebase operations
2. Handles data transformation between Firebase and models
3. Manages error handling and callbacks
4. Ensures data consistency across operations
```

### 31. TEACHER FIREBASE REPOSITORY
**File:** `controllers/teacherFirebaseRepository.kt`
```
Purpose: Firebase operations for teacher management
Key Functions:
- getTeachers(): Loads all teacher profiles
- getTeacherById(): Loads specific teacher
- addTeacher(): Creates new teacher profile
- updateTeacher(): Updates teacher information
- deleteTeacher(): Removes teacher profile

System Flow:
1. Handles all teacher-related Firebase operations
2. Provides callback-based async operations
3. Maps Firebase documents to teacherModel
4. Used by teacher management screens
```

### 32. USER FIREBASE REPOSITORY
**File:** `controllers/userFirebaseRepository.kt`
```
Purpose: Firebase operations for user/member management
Key Functions:
- getUsers(): Loads all user profiles
- getUserById(): Loads specific user
- addUser(): Creates new user profile
- updateUser(): Updates user information
- deleteUser(): Removes user profile

System Flow:
1. Manages all user-related Firebase operations
2. Handles member data persistence
3. Provides async callbacks for UI updates
4. Ensures data consistency for user operations
```

---

## 🎨 UI THEME AND STYLING

### 33. THEME CONFIGURATION
**File:** `ui/theme/Theme.kt`
```
Purpose: Material3 theme configuration
Key Features:
- Dark and light theme support
- Dynamic color scheme
- Material3 design system integration
- Consistent color palette

System Flow:
1. Wraps entire app in theme
2. Provides consistent styling
3. Supports system theme switching
4. Material3 color roles applied
```

### 34. COLOR SCHEME
**File:** `ui/theme/Color.kt`
```
Purpose: App color palette definition
Components:
- Primary colors for main UI elements
- Secondary colors for accents
- Surface colors for cards and backgrounds
- Error colors for validation states

System Flow:
1. Defines app-wide color constants
2. Used by MaterialTheme
3. Ensures visual consistency
4. Supports accessibility contrast ratios
```

### 35. TYPOGRAPHY CONFIGURATION
**File:** `ui/theme/Type.kt`
```
Purpose: Text styling and font configuration
Components:
- Font family definitions
- Text size scale (headings, body, labels)
- Font weight variations
- Line height specifications

System Flow:
1. Provides consistent text styling
2. Material3 typography scale
3. Used throughout app components
4. Ensures readability and hierarchy
```

---

## 🧩 REUSABLE COMPONENTS

### 36. WHEEL DATE TIME PICKER
**File:** `components/WheelDateTimePickerDialog.kt`
```
Purpose: Custom date/time picker with wheel interface
Key Features:
- Wheel-style date selection
- Time picker integration
- Material3 dialog design
- Day-of-week validation support

System Flow:
1. Triggered from date input fields
2. Provides intuitive date selection
3. Validates against business rules
4. Returns selected date to parent
```

### 37. DROPDOWN MENU COMPONENT
**File:** `components/DropdownMenu.kt`
```
Purpose: Reusable dropdown selection component
Key Features:
- Generic dropdown implementation
- Custom styling options
- Keyboard navigation support
- Material3 design integration

System Flow:
1. Used in forms for option selection
2. Provides consistent dropdown behavior
3. Handles selection state management
4. Integrates with form validation
```

### 38. DETAIL SECTION COMPONENT
**File:** `components/DetailSection.kt`
```
Purpose: Standardized section for displaying information
Key Features:
- Consistent layout for detail views
- Section headers and content areas
- Material3 card styling
- Responsive design

System Flow:
1. Used in detail dialogs and screens
2. Provides consistent information layout
3. Supports various content types
4. Maintains visual hierarchy
```

### 39. DETAIL ITEM COMPONENT
**File:** `components/DetailItem.kt`
```
Purpose: Individual key-value pair display component
Key Features:
- Label and value display
- Icon support
- Consistent spacing and typography
- Material3 styling

System Flow:
1. Used within DetailSection components
2. Shows individual data points
3. Provides consistent formatting
4. Supports icons and various data types
```

---

## 🧪 TESTING INFRASTRUCTURE

### 40. UNIT TESTS
**File:** `src/test/java/com/example/comp1786_su25/ExampleUnitTest.kt`
```
Purpose: Unit testing framework setup
Key Features:
- JUnit testing framework
- Local unit tests for business logic
- Repository testing
- ViewModel testing setup

System Flow:
1. Tests run in local JVM
2. Validates business logic
3. Ensures code reliability
4. Catches regressions early
```

### 41. INSTRUMENTATION TESTS
**File:** `src/androidTest/java/com/example/comp1786_su25/ExampleInstrumentedTest.kt`
```
Purpose: Android instrumentation testing
Key Features:
- UI testing framework
- Device/emulator testing
- Integration testing
- Context-dependent tests

System Flow:
1. Tests run on Android device/emulator
2. Validates UI interactions
3. Tests Firebase integration
4. Ensures app functionality
```

---

## 📊 SYSTEM FLOW SUMMARY

### Complete User Journey:
1. **App Launch** → MainActivity → MyAppNavigation → IntroPage
2. **Authentication** → LoginPage/SignupPage → AuthViewModel → Firebase Auth
3. **Main Dashboard** → HomePage → TabView → Three main sections
4. **Class Management** → ClassPage → CourseCard → Add/Update/Delete operations
5. **Teacher Management** → TeacherPage → TeacherCard → CRUD operations
6. **User Management** → UserPage → UserCard → Profile management
7. **Data Persistence** → Firebase Repositories → Firestore database
8. **Real-time Updates** → LiveData → UI state updates

### Key Design Patterns:
- **MVVM Architecture**: ViewModels manage UI state
- **Repository Pattern**: Abstracts data access layer
- **Observer Pattern**: LiveData for reactive UI updates
- **Navigation Component**: Type-safe navigation with parameters
- **Material3 Design**: Consistent UI/UX following Material guidelines
- **Composition**: Jetpack Compose for declarative UI
- **Single Source of Truth**: Firebase as primary data store

### Technical Highlights:
- **Real-time Data Sync**: Firebase Firestore integration
- **Type Safety**: Kotlin data classes and sealed classes
- **State Management**: Compose state and ViewModels
- **Navigation**: Deep linking with parameter passing
- **Error Handling**: Comprehensive error states and user feedback
- **Performance**: Lazy loading and efficient list rendering
- **Accessibility**: Material3 accessibility standards
- **Testing**: Unit and instrumentation testing framework

This comprehensive system demonstrates modern Android development practices with a robust architecture suitable for real-world gym management operations.

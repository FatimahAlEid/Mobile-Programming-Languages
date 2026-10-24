# Bride Line System Architecture

Bride Line uses a layered architecture consisting of UI, Domain, and Data layers. Each layer has a specific responsibility, keeping screen presentation separate from application logic and data storage.

## Architecture Diagram

![Bride Line System Architecture](docs/Bride_Line_System_Architecture.jpeg)

## 1. UI Layer

The UI layer uses Jetpack Compose to display screens and handle user interactions.

The screens cover:
- Account and profile.
- Home screens.
- Service browsing and reviews.
- Appointments and chat.
- Budget and payments.
- Provider dashboard and calendar.
- Bridal shower planning and shared tasks.

### ViewModels and UI State

The UI layer includes:
- AuthViewModel
- ProfileViewModel
- ServicesViewModel
- BookingViewModel
- ChatViewModel
- BudgetViewModel
- PaymentViewModel
- ProviderViewModel
- TasksViewModel

Composable screens send UI events to their ViewModels. The ViewModels process these events, request the required operations, and expose UiState through StateFlow.

The screens observe UiState and update their displayed content accordingly.

## 2. Domain Layer

The Domain layer defines application operations, domain models, and repository interfaces.

### Use Cases

Use cases represent specific application actions:

| Use case | Responsibility |
| --- | --- |
| LoginUseCase | Handles user login. |
| RegisterAccountUseCase | Handles account registration. |
| UpdateProfileUseCase | Updates user profile information. |
| SubmitReviewUseCase | Submits a service review. |
| BookAppointmentUseCase | Books an appointment. |
| CancelAppointmentUseCase | Cancels an appointment. |
| UpdateBudgetUseCase | Updates wedding budget information. |
| RecordPaymentUseCase | Records payment information. |
| BookingRequestUseCase | Handles provider booking requests. |
| UpdateTaskUseCase | Updates shared task information. |

### Repository Interfaces

Repository interfaces define the data operations available to the application without specifying how data is stored.

The interfaces are:
- AuthRepository
- ProfileRepository
- ServiceRepository
- BookingRepository
- ChatRepository
- BudgetRepository
- PaymentRepository
- TaskRepository

### Domain Models

The domain models represent the main application entities:
- User
- ServiceProvider
- Appointment
- Review
- Message
- Budget
- Payment
- SharedTask

## 3. Data Layer

The Data layer implements the repository interfaces and manages access to local data sources.

### Repository Implementations

The implementations are:
- AuthRepositoryImpl
- ProfileRepositoryImpl
- ServiceRepositoryImpl
- BookingRepositoryImpl
- ChatRepositoryImpl
- BudgetRepositoryImpl
- PaymentRepositoryImpl
- TaskRepositoryImpl

These implementations perform data operations through the appropriate data source and return models, results, or Flow streams.

### Local Database

LocalDataSource uses Room DAOs and AppDatabase to read and write local records and cached provider data.

DAOs define database access operations, while AppDatabase provides access to the database and its DAOs.

### Preferences

PreferencesDataSource uses Preferences DataStore to save notification preferences and other small application settings.

# 架构设计

本应用采用 **MVVM (Model-View-ViewModel)** 架构，并结合了 **Repository** 和 **Clean Architecture** 的思想，以实现模块化、可测试和可维护的代码。

## 架构图

![Architecture Diagram](resource/screenshot/architecture.png)

## 各层职责

### 1. UI 层 (View)

*   **组件**: Jetpack Compose UI, Activities, Fragments
*   **职责**:
    *   负责应用的 UI 展示和用户交互。
    *   通过观察 ViewModel 中的状态来更新 UI。
    *   将用户操作通知给 ViewModel。

### 2. ViewModel 层

*   **组件**: ViewModels
*   **职责**:
    *   持有和管理与 UI 相关的数据（UI State）。
    *   处理 UI 层的用户操作，执行业务逻辑。
    *   通过调用 Repository 层来获取数据。
    *   将数据转换为 UI 可以直接使用的状态。

### 3. Repository 层

*   **组件**: Repository
*   **职责**:
    *   作为数据访问的统一入口，为 ViewModel 提供数据。
    *   负责协调从远程数据源（网络）和本地数据源（数据库）获取数据。
    *   实现数据缓存策略，例如在网络请求失败时返回缓存数据。

### 4. 数据层 (Data)

*   **组件**:
    *   **远程数据源**: Retrofit, OkHttp
    *   **本地数据源**: Room Database
*   **职责**:
    *   **远程数据源**: 负责通过网络从 GitHub API 和 OpenRouter API 获取数据。
    *   **本地数据源**: 负责将数据缓存到本地数据库，以实现离线访问和提高性能。

## 数据流

1.  **UI 层** 发起一个操作 (e.g., 点击按钮)。
2.  **ViewModel** 接收到操作，并调用 **Repository** 的相应方法来获取数据。
3.  **Repository** 决定是从 **远程数据源** 还是 **本地数据源** 获取数据。
    *   如果需要从网络获取，则通过 **Retrofit** 调用 API。
    *   获取到的数据可以缓存到 **Room** 数据库。
    *   如果网络不可用，可以从 **Room** 数据库读取缓存数据。
4.  **Repository** 将获取到的数据返回给 **ViewModel**。
5.  **ViewModel** 将数据处理成 UI State，并通过 `StateFlow` 或 `LiveData` 暴露给 **UI 层**。
6.  **UI 层** 观察到 UI State 的变化，并更新界面。

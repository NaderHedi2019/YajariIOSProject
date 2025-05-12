import SwiftUI
import shared

struct ChatListView: View {
    @Environment(\._chatRepository) private var chatRepository
    @State private var chats: [Chat] = []
    @State private var searchText = ""
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    
    var filteredChats: [Chat] {
        if searchText.isEmpty {
            return chats
        }
        return chats.filter { chat in
            chat.participantName.localizedCaseInsensitiveContains(searchText) ||
            chat.lastMessage.localizedCaseInsensitiveContains(searchText)
        }
    }
    
    var body: some View {
        Group {
            if isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
            } else if chats.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "message.slash")
                        .font(.system(size: 60))
                        .foregroundColor(.gray)
                    Text("No chats yet")
                        .font(.title2)
                        .foregroundColor(.gray)
                    Text("Your conversations will appear here")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            } else {
                List {
                    ForEach(filteredChats, id: \.id) { chat in
                        NavigationLink(destination: ChatDetailView(chat: chat)) {
                            ChatRowView(chat: chat)
                        }
                    }
                }
                .searchable(text: $searchText, prompt: "Search chats")
            }
        }
        .navigationTitle("Chats")
        .alert("Error", isPresented: $showError) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(errorMessage)
        }
        .onAppear {
            loadChats()
        }
    }
    
    private func loadChats() {
        isLoading = true
        
        chatRepository.getChats { result in
            isLoading = false
            
            switch result {
            case .success(let chats):
                self.chats = chats
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
}

struct ChatRowView: View {
    let chat: Chat
    
    var body: some View {
        HStack(spacing: 12) {
            // Profile Image
            AsyncImage(url: URL(string: chat.participantImageUrl)) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Image(systemName: "person.circle.fill")
                    .foregroundColor(.gray)
            }
            .frame(width: 50, height: 50)
            .clipShape(Circle())
            
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(chat.participantName)
                        .font(.headline)
                    
                    Spacer()
                    
                    Text(formatDate(chat.lastMessageTime))
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Text(chat.lastMessage)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
        }
        .padding(.vertical, 4)
    }
    
    private func formatDate(_ timestamp: Double) -> String {
        let date = Date(timeIntervalSince1970: timestamp)
        let formatter = DateFormatter()
        
        if Calendar.current.isDateInToday(date) {
            formatter.dateFormat = "HH:mm"
        } else if Calendar.current.isDateInYesterday(date) {
            return "Yesterday"
        } else {
            formatter.dateFormat = "dd/MM/yy"
        }
        
        return formatter.string(from: date)
    }
}

struct ChatDetailView: View {
    let chat: Chat
    @Environment(\._chatRepository) private var chatRepository
    @State private var messageText = ""
    @State private var messages: [Message] = []
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    
    var body: some View {
        VStack {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(messages, id: \.id) { message in
                        MessageBubbleView(message: message)
                    }
                }
                .padding()
            }
            
            HStack {
                TextField("Type a message...", text: $messageText)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .padding(.horizontal)
                
                Button(action: sendMessage) {
                    Image(systemName: "paperplane.fill")
                        .foregroundColor(.blue)
                }
                .padding(.trailing)
                .disabled(messageText.isEmpty)
            }
            .padding(.vertical, 8)
            .background(Color(.systemBackground))
            .shadow(radius: 1)
        }
        .navigationTitle(chat.participantName)
        .alert("Error", isPresented: $showError) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(errorMessage)
        }
        .onAppear {
            loadMessages()
        }
    }
    
    private func loadMessages() {
        isLoading = true
        
        chatRepository.getMessages(chatId: chat.id) { result in
            isLoading = false
            
            switch result {
            case .success(let messages):
                self.messages = messages
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
    
    private func sendMessage() {
        guard !messageText.isEmpty else { return }
        
        let message = Message(
            id: UUID().uuidString,
            chatId: chat.id,
            senderId: "current_user_id", // Replace with actual user ID
            content: messageText,
            timestamp: Date().timeIntervalSince1970,
            isRead: false
        )
        
        chatRepository.sendMessage(message: message) { result in
            switch result {
            case .success:
                messageText = ""
                messages.append(message)
            case .failure(let error):
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
}

struct MessageBubbleView: View {
    let message: Message
    let isCurrentUser = true // Replace with actual user check
    
    var body: some View {
        HStack {
            if isCurrentUser {
                Spacer()
            }
            
            Text(message.content)
                .padding()
                .background(isCurrentUser ? Color.blue : Color(.systemGray5))
                .foregroundColor(isCurrentUser ? .white : .primary)
                .cornerRadius(16)
            
            if !isCurrentUser {
                Spacer()
            }
        }
    }
} 
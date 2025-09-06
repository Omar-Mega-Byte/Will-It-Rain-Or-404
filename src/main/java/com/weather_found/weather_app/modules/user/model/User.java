package com.weather_found.weather_app.modules.user.model;

import java.util.List;

import com.weather_found.weather_app.modules.event.model.Event;
import com.weather_found.weather_app.modules.shared.Base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users", indexes = {
                @jakarta.persistence.Index(name = "idx_username", columnList = "username"),
                @jakarta.persistence.Index(name = "idx_email", columnList = "email")
})
public class User extends BaseEntity {
        @Column(nullable = false, unique = true)
        private String username;

        @Column(nullable = false, unique = true)
        private String email;

        @Column(nullable = false)
        private String password;

        @Column(nullable = false)
        private String firstName;

        @Column(nullable = false)
        private String lastName;

        @Column(nullable = false)
        private boolean isActive;

        @ManyToMany(fetch = jakarta.persistence.FetchType.LAZY)
        @JoinTable(name = "user_events", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "event_id"))
        private List<Event> events;

        // Default constructor
        public User() {
                super();
        }

        // Constructor with all fields
        public User(String username, String email, String password, String firstName, String lastName,
                        boolean isActive) {
                super();
                this.username = username;
                this.email = email;
                this.password = password;
                this.firstName = firstName;
                this.lastName = lastName;
                this.isActive = isActive;
                this.events = new java.util.ArrayList<>();
        }

        // Getters and Setters
        public String getUsername() {
                return username;
        }

        public void setUsername(String username) {
                this.username = username;
        }

        public String getEmail() {
                return email;
        }

        public void setEmail(String email) {
                this.email = email;
        }

        public String getPassword() {
                return password;
        }

        public void setPassword(String password) {
                this.password = password;
        }

        public String getFirstName() {
                return firstName;
        }

        public void setFirstName(String firstName) {
                this.firstName = firstName;
        }

        public String getLastName() {
                return lastName;
        }

        public void setLastName(String lastName) {
                this.lastName = lastName;
        }

        public boolean isActive() {
                return isActive;
        }

        public void setActive(boolean isActive) {
                this.isActive = isActive;
        }

        public List<Event> getEvents() {
                return events;
        }

        public void setEvents(List<Event> events) {
                this.events = events;
        }

        @Override
        public boolean equals(Object obj) {
                if (this == obj)
                        return true;
                if (obj == null || getClass() != obj.getClass())
                        return false;
                if (!super.equals(obj))
                        return false;

                User user = (User) obj;

                if (isActive != user.isActive)
                        return false;
                if (username != null ? !username.equals(user.username) : user.username != null)
                        return false;
                if (email != null ? !email.equals(user.email) : user.email != null)
                        return false;
                if (password != null ? !password.equals(user.password) : user.password != null)
                        return false;
                if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null)
                        return false;
                if (lastName != null ? !lastName.equals(user.lastName) : user.lastName != null)
                        return false;
                return events != null ? events.equals(user.events) : user.events == null;
        }

        @Override
        public int hashCode() {
                int result = super.hashCode();
                result = 31 * result + (username != null ? username.hashCode() : 0);
                result = 31 * result + (email != null ? email.hashCode() : 0);
                result = 31 * result + (password != null ? password.hashCode() : 0);
                result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
                result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
                result = 31 * result + (isActive ? 1 : 0);
                result = 31 * result + (events != null ? events.hashCode() : 0);
                return result;
        }

        @Override
        public String toString() {
                return "User{" +
                                "id=" + getId() +
                                ", username='" + username + '\'' +
                                ", email='" + email + '\'' +
                                ", password='[PROTECTED]'" +
                                ", firstName='" + firstName + '\'' +
                                ", lastName='" + lastName + '\'' +
                                ", isActive=" + isActive +
                                ", events=" + (events != null ? events.size() + " events" : "null") +
                                ", createdAt=" + getCreatedAt() +
                                ", updatedAt=" + getUpdatedAt() +
                                '}';
        }
}

import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import { Eye, EyeSlash } from "react-bootstrap-icons";

const UserProfile = () => {
  const navigate = useNavigate();
  const username = localStorage.getItem("username");
  const role = localStorage.getItem("role");

  const [userData, setUserData] = useState({
    username: "",
    password: "",
    location: "",
    role: "",
  });

  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [showPassword, setShowPassword] = useState(false); // 👁️ Toggle state

  // Base URL depends on role
  const baseURL =
    role === "CONSUMER"
      ? "http://localhost:8090/consumers"
      : "http://localhost:8091/sellers";

  // Fetch user data by username
  useEffect(() => {
    const fetchUser = async () => {
      try {
        const response = await axios.get(`${baseURL}/${username}`);
        setUserData(response.data);
        setLoading(false);
      } catch (err) {
        console.error(err);
        setMessage("Failed to fetch user details.");
        setLoading(false);
      }
    };

    if (username) fetchUser();
  }, [username, baseURL]);

  // Handle input change
  const handleChange = (e) => {
    const { name, value } = e.target;
    setUserData({ ...userData, [name]: value });
  };

  // Update user details (PATCH)
  const handleSave = async () => {
    try {
      const response = await axios.patch(`${baseURL}/${username}`, {
        password: userData.password,
        location: userData.location,
      });
      setUserData(response.data);
      setIsEditing(false);
      setMessage("Profile updated successfully!");
    } catch (err) {
      console.error(err);
      setMessage("Error updating profile.");
    }
  };

  // Delete user account (by ID)
  const handleDelete = async () => {
    const confirmDelete = window.confirm(
      "Are you sure you want to delete your account? This action cannot be undone."
    );
    if (!confirmDelete) return;

    try {
      const idField = role === "CONSUMER" ? "consumerId" : "sellerId";
      await axios.delete(`${baseURL}/${userData[idField]}`);
      localStorage.clear();
      alert("Your account has been deleted successfully.");
      navigate("/register");
    } catch (err) {
      console.error(err);
      alert("Error deleting account. Please try again later.");
    }
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center vh-100">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container py-5" style={{ maxWidth: "600px" }}>
      <div className="card shadow-lg p-4 border-0 rounded-4">
        <h3 className="fw-bold mb-4 text-center">👤 User Profile</h3>

        {message && (
          <div className="alert alert-info text-center py-2">{message}</div>
        )}

        {/* Username */}
        <div className="mb-3">
          <label className="form-label fw-semibold">Username</label>
          <input
            type="text"
            className="form-control"
            value={userData.username || ""}
            disabled
          />
        </div>

        {/* Password with show/hide toggle */}
        <div className="mb-3 position-relative">
          <label className="form-label fw-semibold">Password</label>
          <input
            type={showPassword ? "text" : "password"}
            className="form-control pe-5"
            name="password"
            value={userData.password || ""}
            onChange={handleChange}
            disabled={!isEditing}
          />
          
            <span
              className="position-absolute"
              style={{ cursor: "pointer", color: "#6c757d", left:"95%", top:"55%"}}
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? <EyeSlash size={18} /> : <Eye size={18} />}
            </span>
          
        </div>

        {/* Location */}
        <div className="mb-3">
          <label className="form-label fw-semibold">Location</label>
          <input
            type="text"
            className="form-control"
            name="location"
            value={userData.location || ""}
            onChange={handleChange}
            disabled={!isEditing}
          />
        </div>

        {/* Role */}
        <div className="mb-3">
          <label className="form-label fw-semibold">Role</label>
          <input
            type="text"
            className="form-control"
            value={userData.role || role}
            disabled
          />
        </div>

        {/* Buttons */}
        <div className="d-flex justify-content-between mt-4">
          {isEditing ? (
            <>
              <button className="btn btn-success" onClick={handleSave}>
                Save
              </button>
              <button
                className="btn btn-secondary"
                onClick={() => setIsEditing(false)}
              >
                Cancel
              </button>
            </>
          ) : (
            <>
              <button
                className="btn btn-primary"
                onClick={() => setIsEditing(true)}
              >
                Edit
              </button>
              <button className="btn btn-danger" onClick={handleDelete}>
                Delete Account
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default UserProfile;

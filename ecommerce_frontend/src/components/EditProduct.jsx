import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate, useParams } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";

const EditProduct = () => {
  const { productId } = useParams();
  const navigate = useNavigate();
  const username = localStorage.getItem("username");

  const [formData, setFormData] = useState({
    name: "",
    price: "",
    brand: "",
    category: "",
    description: "",
    quantity: "",
  });

  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(true);

  //Fetch existing product details
  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const response = await axios.get(
          `http://localhost:8091/sellers/${username}/products/${productId}`
        );
        const product = response.data;

        setFormData({
          name: product.name,
          price: product.price,
          brand: product.brand,
          category: product.category,
          description: product.description,
          quantity: product.quantity,
        });

        if (product.imageUrl) {
          setImagePreview(`http://localhost:8092${product.imageUrl}`);
        }

        setLoading(false);
      } catch (err) {
        setError("Failed to load product details.");
        setLoading(false);
      }
    };

    fetchProduct();
  }, [productId, username]);

  // Handle text field changes
  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  //Handle image upload + preview
  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImageFile(file);
      setImagePreview(URL.createObjectURL(file));
    }
  };

  //Validate fields
  const validateForm = () => {
    const { name, price, brand, category, description, quantity } = formData;
    if (!name || !price || !brand || !category || !description || !quantity) {
      setError("All fields are required.");
      return false;
    }
    if (isNaN(price) || price <= 0) {
      setError("Price must be a positive number.");
      return false;
    }
    if (isNaN(quantity) || quantity <= 0) {
      setError("Quantity must be a positive integer.");
      return false;
    }
    return true;
  };

  // Handle form submit (PUT request)
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!validateForm()) return;

    try {
      const form = new FormData();

      if (imageFile) {
        form.append("imageFile", imageFile);
      }

      const updatedProduct = {
        name: formData.name,
        price: formData.price,
        brand: formData.brand,
        category: formData.category,
        description: formData.description,
        quantity: formData.quantity,
        sellerUsername: username,
      };

      form.append(
        "product",
        new Blob([JSON.stringify(updatedProduct)], { type: "application/json" })
      );

      const response = await axios.put(
        `http://localhost:8091/sellers/${username}/products/${productId}`,
        form
      );

      if (response.status === 200) {
        alert("Product updated successfully!");
        navigate("/sellerProducts");
      }
    } catch (err) {
      console.error("Edit product error:", err);
      setError("Failed to update product. Please try again.");
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
    <div className="container my-5 d-flex justify-content-center">
      <div
        className="card shadow-lg p-4"
        style={{ maxWidth: "480px", width: "100%", borderRadius: "12px" }}
      >
        <h3 className="text-center mb-3 fw-bold">Edit Product</h3>
        <p className="text-muted text-center mb-4">
          Modify product details and update changes.
        </p>

        <form onSubmit={handleSubmit}>
          {/* Name */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Product Name</label>
            <input
              type="text"
              name="name"
              className="form-control"
              value={formData.name}
              onChange={handleChange}
              required
            />
          </div>

          {/* Price */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Price (₹)</label>
            <input
              type="number"
              name="price"
              className="form-control"
              value={formData.price}
              onChange={handleChange}
              required
              step="0.01"
            />
          </div>

          {/* Brand */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Brand</label>
            <input
              type="text"
              name="brand"
              className="form-control"
              value={formData.brand}
              onChange={handleChange}
              required
            />
          </div>

          {/* Category */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Category</label>
            <input
              type="text"
              name="category"
              className="form-control"
              value={formData.category}
              onChange={handleChange}
              required
            />
          </div>

          {/* Description */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Description</label>
            <textarea
              name="description"
              className="form-control"
              rows="3"
              value={formData.description}
              onChange={handleChange}
              required
            ></textarea>
          </div>

          {/* Quantity */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Quantity</label>
            <input
              type="number"
              name="quantity"
              className="form-control"
              value={formData.quantity}
              onChange={handleChange}
              required
            />
          </div>

          {/* Image Upload */}
          <div className="mb-4">
            <label className="form-label fw-semibold">Product Image</label>
            <input
              type="file"
              accept="image/*"
              className="form-control"
              onChange={handleImageChange}
            />

            {/* Image Preview */}
            {imagePreview && (
              <div className="text-center mt-3">
                <img
                  src={imagePreview}
                  alt="Preview"
                  style={{
                    width: "150px",
                    height: "150px",
                    objectFit: "contain",
                    border: "1px solid #ddd",
                    borderRadius: "8px",
                    backgroundColor: "#f8f9fa",
                  }}
                />
              </div>
            )}
          </div>

          <button type="submit" className="btn btn-primary w-100 fw-semibold">
            Update Product
          </button>
        </form>

        {error && <div className="alert alert-danger mt-3">{error}</div>}
        {success && <div className="alert alert-success mt-3">{success}</div>}

        <div className="text-center mt-3">
          <p
            className="text-muted"
            style={{ cursor: "pointer", border: "1px solid #ddd" }}
            onClick={() => navigate("/sellerProducts")}
          >
            ← Back to My Products
          </p>
        </div>
      </div>
    </div>
  );
};

export default EditProduct;

import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";

const SellerProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const username = localStorage.getItem("username");

  //Fetch products by seller username
  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await axios.get(
          `http://localhost:8091/sellers/${username}/products`
        );
        setProducts(response.data);
        setLoading(false);
      } catch (err) {
        setError("No Products found.");
        setLoading(false);
      }
    };
    fetchProducts();
  }, [username]);

  //Handle delete with confirmation
  const handleDelete = async (productId) => {
    const confirmDelete = window.confirm("Are you sure you want to delete this product?");
    if (!confirmDelete) return;

    try {
      await axios.delete(
        `http://localhost:8091/sellers/${username}/products/${productId}`
      );
      alert("Product deleted successfully!");
      setProducts(products.filter((p) => p.productId !== productId));
    } catch (err) {
      alert("Failed to delete product. Please try again.");
    }
  };

  if (loading)
    return (
      <div className="d-flex justify-content-center align-items-center vh-100">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );

  if (error) return<> <div className="alert alert-danger text-center mt-5">{error}
  </div>
  <div className="d-flex justify-content-center"> 
    <button
          className="btn btn-primary "
          onClick={() => navigate("/addProduct")}
        >
          + Add Product
        </button>
  </div></>;

  return (
    <div className="container py-5">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2 className="fw-bold">My Products</h2>
        <button
          className="btn btn-primary"
          onClick={() => navigate("/addProduct")}
        >
          + Add Product
        </button>
      </div>

      {products.length === 0 ? (
        <div className="text-center text-muted">
          <p>No products found.</p>
        </div>
      ) : (
        <div className="row">
          {products.map((product) => {
            //Construct local image path directly from uploads folder
            const imageUrl = product.imageUrl
            ? `http://localhost:8092${product.imageUrl}`
            : null;

            return (
              <div className="col-sm-6 col-md-4 col-lg-3 mb-4 " key={product.productId}>
                <div className="card shadow-sm h-100 flex flex-column">
                    {imageUrl ? (
                    <img
                      src={imageUrl}
                      alt={product.name}
                      className="card-img-top"
                      style={{
                        width: "100%",
                        height: "180px",
                        objectFit: "contain",
                        padding: "10px",
                        borderTopLeftRadius: "8px",
                        borderTopRightRadius: "8px",
                        backgroundColor: "#f8f9fa",
                        filter: product.quantity === 0 ? "blur(3px) brightness(0.8)" : "none",
                        transition: "0.3s ease",
                      }}
                    />
                  ) : (
                    <div
                      className="d-flex justify-content-center align-items-center bg-light text-muted"
                      style={{
                        height: "220px",
                        borderTopLeftRadius: "6px",
                        borderTopRightRadius: "6px",
                      }}
                    >
                      No Image
                    </div>
                  )}

                  {product.quantity === 0 && (
                    <div className="bg-warning text-dark fw-bold position-absolute px-3 py-2 rounded shadow"
                    style={{ zIndex: 10,top:"25%",left:"48%", transform: "translate(-50%, -50%)", margin: 0, }}
                    > SOLD OUT
                    </div>
                  )}

                  {/*Product Details */}
                  <div className="card-body d-flex flex-column justify-content-between">
                    <h5 className="card-title fw-bold">{product.name}</h5>
                    <p className="card-text mb-1">
                      <b>Category:</b> {product.category}
                    </p>
                    <p className="card-text mb-1">
                      <b>Brand:</b> {product.brand}
                    </p>
                    <p className="card-text mb-1">
                      <b>Price:</b> ₹{product.price}
                    </p>
                    <p className="card-text mb-1">
                      <b>Stock:</b> {product.quantity}
                    </p>
                    <p className="card-text mb-1">
                      <b>Description:</b> {product.description}
                    </p>

                    <div className="d-flex justify-content-between mt-3">
                      <button
                        className="btn btn-warning btn-sm text-white px-3"
                        onClick={() =>
                          navigate(`/editProduct/${product.productId}`)
                        }
                      >
                        Edit
                      </button>
                      <button
                        className="btn btn-danger btn-sm px-3"
                        onClick={() => handleDelete(product.productId)}
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default SellerProducts;

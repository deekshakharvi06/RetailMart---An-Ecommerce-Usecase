import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft } from "react-bootstrap-icons";

const Wishlist = () => {
  const [wishlist, setWishlist] = useState([]);
  const navigate = useNavigate();
  const username = localStorage.getItem("username");
  useEffect(() => {
    const savedWishlist = JSON.parse(localStorage.getItem(`wishlist_${username}`)) || [];
    setWishlist(savedWishlist);
  }, []);

  const removeFromWishlist = (productId) => {
    const updated = wishlist.filter((item) => item.productId !== productId);
    setWishlist(updated);
    localStorage.setItem(`wishlist_${username}`, JSON.stringify(updated));
  };

  return (
    <>
    <div className="border-1 d-flex align-items-left">
            <button
              className="btn btn-link text-decoration-none text-dark mb-3 p-0 border-1"
              onClick={() => navigate("/ConsumerProducts")}
            >
              <ArrowLeft /> Back to Products
            </button>
          </div>
    <div className="container py-5">
      <h2 className="fw-bold mb-4">My Wishlist</h2>
      {wishlist.length === 0 ? (
        <div className="text-center text-muted">
          <p>No items in your wishlist.</p>
        </div>
      ) : (
        <div className="row">
          {wishlist.map((product) => (
            <div
              key={product.productId}
              className="col-12 col-sm-6 col-md-4 col-lg-3 mb-4"
            >
              <div
                className="card shadow-sm h-100 position-relative"
                style={{ cursor: "pointer" }}
                onClick={() =>
                  navigate(`/viewProductDetails/${product.productId}`)
                }
              >
                {/* Product Image */}
                <div
                  className="d-flex justify-content-center align-items-center bg-light"
                  style={{
                    height: "180px",
                    borderTopLeftRadius: "8px",
                    borderTopRightRadius: "8px",
                    backgroundColor: "#f8f9fa",
                  }}
                >
                  <img
                    src={`http://localhost:8092${product.imageUrl}`}
                    alt={product.name}
                    className="img-fluid"
                    style={{
                      width: "100%",
                      height: "100%",
                      objectFit: "contain",
                      padding: "10px",
                    }}
                  />
                </div>

                {/* Product Info */}
                <div className="card-body d-flex flex-column justify-content-between">
                  <h5 className="card-title fw-bold">{product.name}</h5>
                  <p className="card-text fw-semibold mb-1">₹{product.price}</p>

                  {/*Remove button  */}
                  <div className="d-flex justify-content-end mt-2">
                    <button
                      className="btn btn-outline-danger btn-sm"
                      onClick={(e) => {
                        e.stopPropagation(); // prevent navigation
                        removeFromWishlist(product.productId);
                      }}
                    >
                      Remove
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
    </>
  );
};

export default Wishlist;

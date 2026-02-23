
import React, { useEffect, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import { Heart, HeartFill, ArrowLeft } from "react-bootstrap-icons";

const ViewProductDetails = () => {
  const { productId } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [isWishlisted, setIsWishlisted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [relatedProducts, setRelatedProducts] = useState([]);
  const username = localStorage.getItem("username");

  // Fetch product details
  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const response = await axios.get(`http://localhost:8090/consumers/products/${productId}`);
        setProduct(response.data);
        const wishlist = JSON.parse(localStorage.getItem(`wishlist_${username}`)) || [];
        setIsWishlisted(wishlist.some((item) => item.productId === response.data.productId));
      } catch (err) {
        console.error("Error fetching product:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [productId, username]);

  // Log user view activity (NEW)
  const hasLogged = useRef({});
  useEffect(() => {
    if (username && productId && !hasLogged.current[productId]) {
      hasLogged.current[productId] = true;
      axios
        .post("http://localhost:8095/recommendations/activity", {
          username,
          productId,
          action: "view",
        })
        .catch((err) => console.log("Error logging view activity:", err));
    }
  }, [username, productId]);

  // Fetch related products
  useEffect(() => {
    const fetchRelated = async () => {
      try {
        const relatedRes = await axios.get(`http://localhost:8095/recommendations/related/${productId}`);
        let relatedIds = relatedRes.data?.productIds || [];

        // If backend gives empty array, fallback to same category/brand
        if (relatedIds.length === 0 && product) {
          const allRes = await axios.get("http://localhost:8090/consumers/products");
          const fallback = allRes.data.filter(
            (p) =>
              p.productId !== product.productId &&
              (p.name?.toLowerCase().includes(product.name?.split(" ")[0]?.toLowerCase()) ||
                p.category?.toLowerCase() === product.category?.toLowerCase() ||
                p.brand?.toLowerCase() === product.brand?.toLowerCase())
          );
          setRelatedProducts(fallback.slice(0, 12)); // show up to 12 similar ones
          return;
        }

        // Fetch full product details for related IDs
        const relatedPromises = relatedIds.map((id) =>
          axios.get(`http://localhost:8090/consumers/products/${id}`).then((r) => r.data)
        );
        const relatedProductsData = await Promise.all(relatedPromises);
        setRelatedProducts(relatedProductsData.filter((p) => p.productId !== Number(productId)).slice(0, 6));
      } catch (err) {
        console.error("Error fetching related products:", err);
      }
    };

    // Fetch related only after main product is loaded
    if (product) {
      fetchRelated();
    }
  }, [product, productId]);

  const toggleWishlist = () => {
    if (!username) {
      alert("Please log in to use wishlist.");
      return;
    }
    let wishlist = JSON.parse(localStorage.getItem(`wishlist_${username}`)) || [];
    if (isWishlisted) {
      wishlist = wishlist.filter((item) => item.productId !== product.productId);
    } else {
      wishlist.push(product);
      axios
        .post("http://localhost:8095/recommendations/activity", {
          username,
          productId,
          action: "like",
        })
        .catch((err) => console.log(err));
    }
    localStorage.setItem(`wishlist_${username}`, JSON.stringify(wishlist));
    setIsWishlisted(!isWishlisted);
  };

  const handlePlaceOrder = async () => {
    try {
      const orderPayload = {
        productId: product.productId,
        quantity,
        price: product.price,
      };
      const response = await axios.post(
        `http://localhost:8090/consumers/${username}/orders/placeOrder`,
        orderPayload
      );
      if (response.status === 200 || response.status === 201) {
        alert("Order placed successfully!");
        axios
          .post("http://localhost:8095/recommendations/activity", {
            username,
            productId,
            action: "order",
          })
          .catch((err) => console.log(err));
        navigate("/consumerOrders");
      }
    } catch (err) {
      console.error("Failed to place order:", err);
      alert("Error placing order. Please check console.");
    }
  };

  if (loading)
    return (
      <div className="text-center mt-5">
        <div className="spinner-border text-primary"></div>
      </div>
    );

  if (!product)
    return (
      <div className="container mt-5 text-center">
        <h5>Product not found.</h5>
      </div>
    );

  const imageUrl = product.imageUrl ? `http://localhost:8092${product.imageUrl}` : null;
  const isOutOfStock = product.quantity === 0;

  const handleIncrease = () => {
    if (quantity < product.quantity) setQuantity(quantity + 1);
    else alert(`Only ${product.quantity} stock available.`);
  };

  const handleDecrease = () => {
    if (quantity > 1) setQuantity(quantity - 1);
  };

  return (
    <>
      <div className="border-1 d-flex align-items-left">
        <button
          className="btn btn-link text-decoration-none text-dark mb-3 p-0 border-1"
          onClick={() => navigate("/consumerProducts")}
        >
          <ArrowLeft /> Back to Products
        </button>
      </div>

      <div className="container py-5">
        <div className="card shadow-sm border-1 mx-auto" style={{ maxWidth: "800px" }}>
          <div className="row g-0">
            {/* LEFT: Image */}
            <div
              className="col-md-5 bg-light d-flex align-items-center justify-content-center p-4 position-relative"
              style={{ minHeight: "400px" }}
            >
              {imageUrl ? (
                <>
                  <img
                    src={imageUrl}
                    alt={product.name}
                    className="img-fluid rounded"
                    style={{
                      maxHeight: "350px",
                      objectFit: "contain",
                      filter: isOutOfStock ? "blur(3px) brightness(0.8)" : "none",
                    }}
                  />
                  {isOutOfStock && (
                    <div className="position-absolute top-50 start-50 translate-middle bg-warning text-dark fw-bold px-4 py-2 rounded shadow">
                      OUT OF STOCK
                    </div>
                  )}
                </>
              ) : (
                <div className="text-muted">No Image Available</div>
              )}
            </div>

            {/* RIGHT: Details */}
            <div className="col-md-7">
              <div className="card-body p-4 d-flex flex-column h-100">
                <div className="d-flex justify-content-between align-items-start">
                  <div>
                    <small className="text-muted fw-bold">Brand : {product.brand}</small>
                    <h2 className="fw-bold mt-1">{product.name}</h2>
                  </div>
                  <button
                    className="btn btn-outline-danger border-0"
                    onClick={toggleWishlist}
                  >
                    {isWishlisted ? <HeartFill size={22} /> : <Heart size={22} />}
                  </button>
                </div>
                <h3 className="fw-bold text-success mb-3">₹{product.price}</h3>
                <p className="text-muted mb-4" style={{ lineHeight: "1.6" }}>
                  {product.description}
                </p>

                <div className="mt-auto">
                  <div className="d-flex align-items-center mb-4">
                    <label className="me-3 fw-bold">Quantity:</label>
                    <div className="input-group" style={{ width: "130px" }}>
                      <button
                        className="btn btn-outline-secondary btn-sm"
                        onClick={handleDecrease}
                        disabled={isOutOfStock}
                      >
                        -
                      </button>
                      <input
                        type="text"
                        className="form-control form-control-sm text-center bg-white"
                        value={quantity}
                        readOnly
                      />
                      <button
                        className="btn btn-outline-secondary btn-sm"
                        onClick={handleIncrease}
                        disabled={isOutOfStock}
                      >
                        +
                      </button>
                    </div>
                  </div>
                  <button
                    className={`btn fw-bold w-100 ${isOutOfStock ? "btn-secondary" : "btn-warning"}`}
                    disabled={isOutOfStock}
                    onClick={handlePlaceOrder}
                  >
                    {isOutOfStock ? "OUT OF STOCK" : "PLACE ORDER"}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ---------- SIMILAR PRODUCTS SECTION ---------- */}
      {relatedProducts.length > 0 && (
        <div className="container mb-5">
          <h3 className="fw-bold mb-4 text-center">Similar Products</h3>
          <div className="row">
            {relatedProducts.map((p) => {
              const img = p.imageUrl ? `http://localhost:8092${p.imageUrl}` : null;
              const isOutOfStockRelated = p.quantity === 0;
              return (
                <div
                  key={p.productId}
                  className="col-6 col-md-4 col-lg-2 mb-4"
                  onClick={() => navigate(`/viewProductDetails/${p.productId}`)}
                  style={{ cursor: "pointer" }}
                >
                  <div className="card shadow-sm h-100 position-relative">
                    {img ? (
                      <img
                        src={img}
                        alt={p.name}
                        className="card-img-top"
                        style={{
                          width: "100%",
                          height: "180px",
                          objectFit: "contain",
                          backgroundColor: "#f8f9fa",
                          padding: "10px",
                          filter: isOutOfStockRelated ? "blur(3px) brightness(0.8)" : "none",
                        }}
                      />
                    ) : (
                      <div
                        className="d-flex justify-content-center align-items-center bg-light text-muted"
                        style={{ height: "180px", fontWeight: "500" }}
                      >
                        No Image
                      </div>
                    )}
                    {isOutOfStockRelated && (
                      <div className="position-absolute top-50 start-50 translate-middle bg-warning text-dark fw-bold px-3 py-1 rounded shadow">
                        Currently unavailable
                      </div>
                    )}
                    <div className="card-body text-center">
                      <h6 className="fw-bold mb-1">{p.name}</h6>
                      <p className="text-success fw-semibold mb-0">₹{p.price}</p>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </>
  );
};

export default ViewProductDetails;

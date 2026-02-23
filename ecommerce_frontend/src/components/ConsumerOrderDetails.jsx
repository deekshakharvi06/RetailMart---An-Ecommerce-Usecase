import React, { useEffect, useState } from "react";
import axios from "axios";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft } from "react-bootstrap-icons";

const ConsumerOrderDetails = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const username = localStorage.getItem("username");

  const [order, setOrder] = useState(null);
  const [product, setProduct] = useState(null);
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isCancelling, setIsCancelling] = useState(false);

  const statusSteps = [
    "PLACED",
    "PROCESSING",
    "SHIPPED",
    "OUTFORDELIVERY",
    "DELIVERED",
  ];

  useEffect(() => {
    const fetchAllDetails = async () => {
      try {
        const orderRes = await axios.get(
          `http://localhost:8090/consumers/${username}/orders/${orderId}`
        );
        const orderData = orderRes.data;
        setOrder(orderData);

        if (orderData.productId) {
          const productRes = await axios.get(
            `http://localhost:8090/consumers/products/${orderData.productId}`
          );
          setProduct(productRes.data);
        }

        const logRes = await axios.get(
          `http://localhost:8094/orderStatusLog/${orderId}`
        );
        const sortedLogs = logRes.data.sort(
          (a, b) => new Date(a.timestamp) - new Date(b.timestamp)
        );
        setLogs(sortedLogs);
      } catch (err) {
        console.error("Error fetching details:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchAllDetails();
  }, [orderId, username]);

  const formatDate = (date) => {
    if (!date) return "";
    return new Date(date).toLocaleDateString("en-GB", {
      day: "2-digit",
      month: "long",
      year: "numeric",
    });
  };

  const getLogForStep = (step) =>
    logs.find(
      (log) =>
        log.newStatus?.toUpperCase() === step ||
        log.status?.toUpperCase() === step
    );

  const orderStatus = order?.status?.toUpperCase();
  const currentStatusIndex = statusSteps.indexOf(orderStatus);

  // detect cancelled types
  const isCancelled = orderStatus === "CANCELLED";
  const isCancelledBySeller =
    orderStatus === "CANCEL" || orderStatus === "CANCELLED_BY_SELLER";
  const isDelivered = orderStatus === "DELIVERED";

  const getMessage = (status) => {
    switch (status.toUpperCase()) {
      case "PLACED":
        return "Your order has been placed successfully.";
      case "PROCESSING":
        return "Your order is being packed and prepared.";
      case "SHIPPED":
        return "Your order has been shipped and is on its way.";
      case "OUTFORDELIVERY":
        return "Our delivery partner is out for delivery.";
      case "DELIVERED":
        return "Order delivered! Enjoy your purchase.";
      case "CANCELLED":
        return "You cancelled this order.";
      case "CANCEL":
        return "Your order has been cancelled by the seller.";
      case "CANCELLED_BY_SELLER":
        return "Your order has been cancelled by the seller.";
      default:
        return "Pending...";
    }
  };

  // Cancel Order Function
  const handleCancelOrder = async () => {
  if (!window.confirm("Are you sure you want to cancel this order?")) return;

  setIsCancelling(true);
  try {
    const sellerUsername = product?.sellerUsername;
    if (!sellerUsername) {
      alert("Seller information not found.");
      setIsCancelling(false);
      return;
    }

    // Cancel order in main service
    const res = await axios.put(
      `http://localhost:8091/sellers/${sellerUsername}/orders/${orderId}/updateStatus`,
      { status: "CANCELLED" },
      { validateStatus: () => true }
    );

    if (res.status >= 200 && res.status < 300) {
      try {
        await axios.post(
          `http://localhost:8094/orderStatusLog`,
          {
            orderId,
            oldStatus: order.status,
            newStatus: "CANCELLED",
            message: "Cancelled by Consumer",
            timestamp: new Date().toISOString(),
          },
          { validateStatus: () => true }
        );
      } catch (logErr) {
        console.warn("Could not create log entry:", logErr.message);
      }

      // Show success even if log failed
      alert("Order cancelled successfully!");
      window.location.reload();
    } else {
      alert("Could not cancel order — unexpected server response.");
    }
  } catch (err) {
    console.error("Network error cancelling order:", err);
    alert("Could not connect to server. Please try again later.");
  } finally {
    setIsCancelling(false);
  }
};
if (loading)
  return (
    <div className="d-flex justify-content-center align-items-center vh-100">
      <div className="spinner-border text-success" role="status"></div>
    </div>
  );

if (!order)
  return <div className="text-center mt-5 text-danger fw-bold">Order not found.</div>;
  return (
    <>
      <div className="border-1 d-flex align-items-left">
        <button
          className="btn btn-link text-decoration-none text-dark mb-3 p-0 border-1"
          onClick={() => navigate("/ConsumerOrders")}
        >
          <ArrowLeft /> Back to Orders
        </button>
      </div>

      <div className="container py-5">
        <div className="row g-4">
          {/* LEFT: Product Info */}
          <div className="col-lg-5">
            <div
              className="card shadow-sm border-1 p-4 h-100"
              style={{ borderColor: "#dee2e6", borderRadius: "10px" }}
            >
              <div className="text-center mb-4 bg-light rounded p-3">
                {product?.imageUrl ? (
                  <img
                    src={`http://localhost:8092${product.imageUrl}`}
                    alt={product.name}
                    className="img-fluid"
                    style={{ maxHeight: "250px", objectFit: "contain" }}
                  />
                ) : (
                  <div className="py-5 text-muted">No Image Available</div>
                )}
              </div>

              <h4 className="fw-bold text-primary mb-2">
                {product?.name || "Product"}
              </h4>
              <p className=" fw-bold mb-3">
                Brand:{" "}
                <span className="text-dark fw-normal">
                  {product?.brand || "N/A"}
                </span>
              </p>
                <p className=" fw-bold mb-3">
                Description:{" "}
                <span className="text-dark fw-normal">
                  {product?.description || "N/A"}
                </span>
              </p>
              <div className="mb-2">
                <strong>Quantity:</strong> {order.quantity}
              </div>
              <div className="mb-2">
                <strong>Total Amount:</strong> ₹{order.totalAmount}
              </div>
              <div className="mb-3">
                <strong>Order Date:</strong> {formatDate(order.orderDate)}
              </div>

              {/*Cancelled / Delivered / Cancel Button */}
              {isCancelledBySeller ? (
                <div className="text-danger fw-bold">
                  Cancelled by Seller on{" "}
                  {formatDate(
                    getLogForStep("CANCEL")?.updatedAt ||
                      getLogForStep("CANCELLED_BY_SELLER")?.updatedAt ||
                      order.updatedAt
                  )}
                </div>
              ) : isCancelled ? (
                <div className="text-danger fw-bold">
                  Cancelled on{" "}
                  {formatDate(
                    getLogForStep("CANCELLED")?.updatedAt || order.updatedAt
                  )}
                </div>
              ) : isDelivered ? (
                <div className="text-success fw-bold">
                  Delivered on{" "}
                  {formatDate(
                    getLogForStep("DELIVERED")?.updatedAt || order.updatedAt
                  )}
                </div>
              ) : (
                <button
                  className="btn btn-danger fw-bold w-100"
                  onClick={handleCancelOrder}
                  disabled={isCancelling || isCancelledBySeller || isCancelled}
                >
                  {isCancelling ? "Cancelling..." : "Cancel Order"}
                </button>
              )}
            </div>
          </div>

          {/* RIGHT: Order Tracking */}
          <div className="col-lg-7">
            <div
              className="card shadow-sm border-1 p-4 h-100"
              style={{ borderColor: "#dee2e6", borderRadius: "10px" }}
            >
              <h4 className="fw-bold text-primary mb-4">Order Tracking</h4>

              {/* Cancelled by Seller or Consumer */}
              {(isCancelled || isCancelledBySeller) ? (
                <div className="d-flex gap-3 mb-4">
                  <div className="d-flex flex-column align-items-center">
                    <div
                      className="bg-danger rounded-circle"
                      style={{ width: "16px", height: "16px" }}
                    ></div>
                  </div>
                  <div>
                    <h6 className="fw-bold mb-1 text-danger">
                      Order Cancelled
                    </h6>
                    <small className="text-muted d-block">
                      {formatDate(
                        getLogForStep(isCancelledBySeller ? "CANCEL" : "CANCELLED")
                          ?.updatedAt || order.updatedAt
                      )}
                    </small>
                    <p className="text-muted small mt-1">
                      {getMessage(
                        isCancelledBySeller ? "CANCEL" : "CANCELLED"
                      )}
                    </p>
                  </div>
                </div>
              ) : (
                // Normal Tracking
                statusSteps.map((step, index) => {
                  const log = getLogForStep(step);
                  const isActive = index <= currentStatusIndex;
                  const showLine = index < statusSteps.length - 1;

                  return (
                    <div
                      key={step}
                      className="d-flex gap-3 mb-2 position-relative"
                      style={{ minHeight: "80px" }}
                    >
                      <div
                        className="d-flex flex-column align-items-center"
                        style={{ width: "20px" }}
                      >
                        <div
                          className={`rounded-circle ${
                            isActive ? "bg-success" : "bg-light border"
                          }`}
                          style={{
                            width: "16px",
                            height: "16px",
                            zIndex: 2,
                          }}
                        ></div>
                        {showLine && (
                          <div
                            className={`${
                              index < currentStatusIndex
                                ? "bg-success"
                                : "bg-light border-start"
                            }`}
                            style={{ width: "2px", flexGrow: 1 }}
                          ></div>
                        )}
                      </div>

                      <div className="pb-3">
                        <h6
                          className={`fw-bold mb-1 ${
                            isActive ? "text-dark" : "text-muted"
                          }`}
                        >
                          {step}
                        </h6>
                        {isActive && (
                          <>
                            <small className="text-muted d-block">
                              {formatDate(log?.updatedAt)}
                            </small>
                            <p className="text-muted small mt-1 mb-0">
                              {getMessage(step)}
                            </p>
                          </>
                        )}
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default ConsumerOrderDetails;

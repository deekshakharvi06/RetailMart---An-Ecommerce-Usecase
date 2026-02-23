import React from 'react'
import Register from './components/Register'
import {  BrowserRouter as Router, Routes, Route, Navigate} from 'react-router-dom'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import Home from './components/Home'
import Login from './components/Login'
import SellerProducts from './components/SellerProducts'
import ConsumerProducts from './components/ConsumerProducts'
import SellerOrders from './components/SellerOrders'
import ConsumerOrders from './components/ConsumerOrders'
import AddProduct from './components/AddProduct'
import EditProduct from './components/EditProduct'
import ViewProductDetails from './components/ViewProductDetails'
import ConsumerOrderDetails from './components/ConsumerOrderDetails'
import Wishlist from './components/Wishlist'
import UserProfile from './components/UserProfile'


const App = () => {
  return (
    <div className="d-flex flex-column min-vh-100">
    <Router>
    <Navbar/>
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/register" element={<Register />} />
      <Route path="/login" element={<Login />} />
      <Route path="/sellerProducts" element={<SellerProducts />} />
      <Route path="/consumerProducts" element={<ConsumerProducts />} />
      <Route path="/consumerOrders" element={<ConsumerOrders />} />
      <Route path="/sellerOrders" element={<SellerOrders />} />
      <Route path="/addProduct" element={<AddProduct />} />
      <Route path="/editProduct/:productId" element={<EditProduct />} />
      <Route path="/viewProductDetails/:productId" element={<ViewProductDetails />} />
      <Route path="/viewOrderDetails/:orderId" element={<ConsumerOrderDetails />} />
      <Route path="/wishlist" element={<Wishlist />} />
      <Route path="/userProfile" element={<UserProfile />} />
    </Routes>
    <Footer/>
    </Router>
    </div>
  )
}

export default App

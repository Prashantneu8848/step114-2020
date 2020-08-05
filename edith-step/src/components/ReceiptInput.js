import React from 'react';
import axios from 'axios';
import createReactClass from 'create-react-class';
import PropTypes from 'prop-types';
import 'regenerator-runtime/runtime';

/**
 * Displays form that takes in user input for a dynamic number
 * of grocery items and their prices and quantities and adds them
 * to a list.
 */
export default class ReceiptInput extends React.Component {
  /**
   * Constructor
   * @param {Props} props Setup state.
   */
  constructor(props) {
    super(props);
    this.state = {items: [], itemName: '', itemPrice: 0.0,
      itemCategory: 'category', itemQuantity: 1,
      itemDeal: '', itemExpiration: ''};
    this.getDate = this.getDate.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  /**
   * Calculates the current date in yyyy-mm-dd format
   * @return {string} The current date
   */
  getDate() {
    const date = new Date(Date.now());
    let month = date.getMonth() + 1;
    month < 10 ? month = '0' + month.toString() : month = month.toString();
    let day = date.getDate();
    day < 10 ? day = '0' + day.toString() : day = day.toString();
    const year = date.getFullYear();
    return [year, month, day].join('-');
  }

  /**
   * Sends item info to the receipt-data servlet and
   * responds with the cheapest store for where to buy that item.
   *
   * @param {String} name item name
   * @param {double} price item price
   * @param {number} quantity item quantity
   * @return {Object} newDeal cheapest item
   */
  async getDeal(name, price, quantity) {
    const response = await axios({
      method: 'post',
      url: '/receipt-data',
      data: {
        itemName: name,
        itemPrice: price,
        itemQuantity: quantity,
      },
    });

    const dealItem = response.data;

    const newDeal = dealItem.storeName === 'NO_STORE' ?
      {storeName: 'NO_STORE', storePrice: 0,
        itemExpiration: dealItem.expiration} :
      {storeName: dealItem.storeName, storePrice: dealItem.price,
        itemExpiration: dealItem.expiration};

    return newDeal;
  }

  /**
   * Calls the getDeal function and passes it data on a particular
   * item (name, price, quantity). The helper function returns the cheapest
   * store for the item passed, and the items list in the state is updated
   * to include the deal.
   * @param {Event} e Submission event.
   */
  async handleSubmit(e) {
    e.preventDefault();
    if (this.state.itemName.length === 0) {
      return;
    }

    const newDeal = await this.getDeal(this.state.itemName,
        this.state.itemPrice, this.state.itemQuantity);

    const dealMessage = newDeal.storeName === 'NO_STORE' ||
      newDeal.storePrice > this.state.itemPrice ?
      'No deal found.' :
      `Purchase at ${newDeal.storeName} for $${newDeal.storePrice}.`;

    const expirationMessage = newDeal.itemExpiration === 'NO_EXPIRATION' ?
      'No expiration found.' :
      `${newDeal.itemExpiration}`;

    const newItem = {
      itemName: this.state.itemName,
      itemPrice: this.state.itemPrice,
      itemQuantity: this.state.itemQuantity,
      itemDeal: dealMessage,
      itemExpiration: expirationMessage,
      id: Date.now(),
    };

    axios({
      method: 'post',
      url: '/user-stats-servlet',
      data: {
        itemName: this.state.itemName,
        itemCategory: this.state.itemCategory,
        itemPrice: this.state.itemPrice,
        itemQuantity: this.state.itemQuantity,
        itemDate: this.getDate(),
        itemReceiptId: this.state.itemReceiptId,
        itemCategory: this.state.itemCategory,
      },
    }).catch((err) => {
      console.log(err);
    });

    this.setState((state) => ({
      items: [...state.items, newItem],
      itemName: '',
      itemPrice: 0.0,
      itemQuantity: 1,
      itemDeal: '',
      itemExpiration: '',
    }));
  }

  /**
   * Update state when form changed.
   * @param {Event} e Change event.
   */
  handleChange(e) {
    const value = e.target.value;
    this.setState({
      [e.target.name]: value,
    });
  }

  /**
   * Render grocery list form and items.
   * @return {React.ReactNode} React virtual DOM
   */
  render() {
    return (
      <div className="container-fluid">
        <h3>Grocery Items</h3>
        <form onSubmit={this.handleSubmit}>
          <div className="form-row">
            <div className="col-auto">
              <div className="input-group mb-2">
                <div className="input-group-prepend">
                  <div className="input-group-text">Item</div>
                </div>
                <input
                  type="text"
                  className="form-control"
                  name="itemName"
                  id="name"
                  value={this.state.itemName}
                  onChange={this.handleChange} />
              </div>
            </div>
            <div className="col-auto">
              <div className="input-group mb-2">
                <div className="input-group-prepend">
                  <div className="input-group-text">Price</div>
                </div>
                <input
                  type="number"
                  className="form-control"
                  name="itemPrice"
                  id="price"
                  step="0.01"
                  value={this.state.itemPrice}
                  onChange={this.handleChange} />
              </div>
            </div>
            <div className="col-auto">
              <div className="input-group mb-2">
                <div className="input-group-prepend">
                  <div className="input-group-text">Quantity</div>
                </div>
                <input
                  type="number"
                  className="form-control"
                  name="itemQuantity"
                  id="quantity"
                  step="1"
                  value={this.state.itemQuantity}
                  onChange={this.handleChange} />
              </div>
            </div>
            <div className="col-auto">
              <button className="btn btn-primary"
                id="submit"
                type="submit"
                value="Submit">Add Item</button>
            </div>
          </div>
        </form>
        {this.state.items.length > 0 &&
          <GroceryList items={this.state.items}/>
        }
      </div>
    );
  }
}

const GroceryList = createReactClass({
  propTypes: {
    items: PropTypes.arrayOf(PropTypes.object),
  },
  /**
   * Render grocery list items.
   * @return {html} grocery list
   */
  render() {
    const props = this.props;
    return (
      <div id="grocery-list" className="list-group col-lg-8">
        <ul className="list-group">
          <li className={
            'h-50 list-group-item d-flex' +
            'justify-content-between align-items-center'}>
            <span className="col-lg-2">Item</span>
            <span className="badge badge-pill col-lg-2">Price</span>
            <span className="badge badge-pill col-lg-2">#</span>
            <span className="badge badge-pill col-lg-4">Deal</span>
            <span className="badge badge-pill col-lg-2">Expiration</span>
          </li>
          {props.items.map((item) => (
            <li className={
              'h-50 list-group-item d-flex' +
              'justify-content-between align-items-center'}
            key={item.id}>
              <span className="item-name col-lg-2">
                {item.itemName}
              </span>
              <span className="item-price badge badge-pill col-lg-2">
                {item.itemPrice}
              </span>
              <span className="item-quantity badge badge-pill col-lg-2">
                {item.itemQuantity}
              </span>
              <span className="item-deal badge badge-pill col-lg-4">
                {item.itemDeal}
              </span>
              <span className="item-expiration badge badge-pill col-lg-2">
                {item.itemExpiration}
              </span>
            </li>
          ))}
        </ul>
      </div>
    );
  },
});

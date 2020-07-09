import React from 'react';

export default class ReceiptInput extends React.Component {
  constructor(props) {
    super(props);
    this.state = {items: [], deals: [], itemName: '', itemPrice: 0.0, itemQuantity: 1};
    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  getDeal(name, price, quantity) {
    const axios = require('axios')
    axios({
      method: 'post',
      url: '/receipt-deals',
      data: {
        itemName: name,
        itemPrice: price,
        itemQuantity: quantity
      }
    }).then(function (response) {
        const dealItem = response.data;
        console.log(dealItem.store);
        const newDeal = {
          storeName: dealItem.store,
          storePrice: dealItem.price,
          storeComment: dealItem.comment,
          id: Date.now()
        };
        console.log(newDeal.storeName);
        return newDeal;
    });
  }

  async handleSubmit(e) {
    e.preventDefault();

    if (this.state.itemName.length === 0) {
      return;
    }
    const newItem = {
      itemName: this.state.itemName,
      itemPrice: this.state.itemPrice,
      itemQuantity: this.state.itemQuantity,
      id: Date.now()
    };

    const newDeal = await this.getDeal(this.state.itemName, this.state.itemPrice, this.state.itemQuantity);
    console.log(newDeal.storeName);

    this.setState(state => ({
      items: state.items.concat(newItem),
      deals: state.deals.concat(newDeal),
      itemName: '',
      itemPrice: 0.0,
      itemQuantity: 1
    }));
  }

  handleChange(e) {
    const value = e.target.value;
    this.setState({
      [e.target.name]: value
    });
  }

  render() {
    return(
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
        <GroceryList items={this.state.items} />
      }
      {this.state.deals.length > 0 &&
        <DealsList items={this.state.deals} />
      }
      </div>
    );
  }
}

var GroceryList = (props) => {
  return (
    <div id="grocery-list">
      <ul className="list-group col-lg-3">
        <li className="h-50 list-group-item d-flex justify-content-between align-items-center">
            <span className="col-lg-1">Item</span>
            <span className="badge badge-pill col-lg-1">Price</span>
            <span className="badge badge-pill col-lg-1">#</span>
          </li>
        {props.items.map(item => (
          <li className="h-50 list-group-item d-flex justify-content-between align-items-center" key={item.id}>
            <span className="item-name col-lg-1">{item.itemName}</span>
            <span className="item-price badge badge-pill col-lg-1">{item.itemPrice}</span>
            <span className="item-quantity badge badge-pill col-lg-1">{item.itemQuantity}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}

var DealsList = (props) => {
  return (
    <div id="deals-list">
      <ul className="list-group col-lg-3">
          <li className="h-50 list-group-item d-flex justify-content-between align-items-center">
            <span className="col-lg-1">Item</span>
            <span className="badge badge-pill col-lg-1">Price</span>
            <span className="badge badge-pill col-lg-1">#</span>
          </li>
        {props.deals.map(deal => (
          <li className="h-50 list-group-item d-flex justify-content-between align-items-center" key={deal.id}>
            <span className="item-name col-lg-1">{deal.storeName}</span>
            <span className="item-price badge badge-pill col-lg-1">{deal.storePrice}</span>
            <span className="item-quantity badge badge-pill col-lg-1">{deal.storeComment}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}

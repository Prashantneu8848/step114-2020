import React from 'react';
import { mount } from 'enzyme';
import ReceiptHandler from './ReceiptHandler';
import './setupTests.js'

let component;
let handleNameChange;
let handlePriceChange;
let handleQuantityChange;
let handleStoreChange;
let getReceiptData;
let addItem;

beforeEach(() => {
  handleNameChange = jest.spyOn(ReceiptHandler.prototype, 'handleNameChange');
  handlePriceChange = jest.spyOn(ReceiptHandler.prototype, 'handlePriceChange');
  handleQuantityChange = jest.spyOn(ReceiptHandler.prototype, 'handleQuantityChange');
  handleStoreChange = jest.spyOn(ReceiptHandler.prototype, 'handleStoreChange');

  getReceiptData = jest.spyOn(ReceiptHandler.prototype, 'getReceiptData');
  addItem = jest.spyOn(ReceiptHandler.prototype, 'addItem');
  component = mount(<ReceiptHandler />);
})

afterEach(() => {
  component.unmount();
});

it('renders properly', () => {
  expect(component.exists()).toBe(true);
});

it('calls getReceiptData on mount', () => {
  expect(getReceiptData).toBeCalled();
});

it('should call appropriate change function on form change', () => {
  const newItem = {
    name: '',
    price: 0.0,
    quantity: 1
  }
  component.setState({ items: newItem });
  expect(component.state('items')).toBe(newItem);

  const promise = new Promise(getReceiptData);
  promise.then(() => {
    component.find('.name').simulate('change');
    expect(handleNameChange).toBeCalled();
    component.find('.price').simulate('change');
    expect(handlePriceChange).toBeCalled();
    component.find('.quantity').simulate('change');
    expect(handleQuantityChange).toBeCalled();
    component.find('.store-name').simulate('change');
    expect(handleStoreChange).toBeCalled();
  });
});

it('should change state when on change functions are called', () => {
  const newItem = {
    name: '',
    price: 0.0,
    quantity: 1
  }
  component.setState({ items: newItem });
  expect(component.state('items')).toBe(newItem);

  const textEvent = {target: { name: "items[0].name", value: 'bread' }};
  const priceEvent = {target: { name: "items[0].price", value: 5.6 }};
  const quantityEvent = {target: {name: "items[0].quantity", value: 3}};
  const storeEvent = {target: {name: "storeName", value: 'Whole Foods'}};

  const targetItem = {
    name: 'bread',
    price: 5.6,
    quantity: 3
  }
  const promise = new Promise(getReceiptData);
  promise.then(() => {
    component.find('.name').simulate('change', textEvent);
    component.find('.price').simulate('change', priceEvent);
    component.find('.quantity').simulate('change', quantityEvent);
    component.find('.store-name').simulate('change', storeEvent)
    expect(component.state('items')).toBe(targetItem);
    expect(component.state('storeName')).toBe('Whole Foods');
  });
});

it('should create a new form field when addItem is called', () => {
  expect(component.find('.name').exists()).toBe(false);
  expect(component.find('.price').exists()).toBe(false);
  expect(component.find('.quantity').exists()).toBe(false);

  const promise = new Promise(getReceiptData);
  promise.then(() => {
    component.find('#add').simulate('click');
    expect(component.find('.name').exists()).toBe(true);
    expect(component.find('.price').exists()).toBe(true);
    expect(component.find('.quantity').exists()).toBe(true);
  });
});

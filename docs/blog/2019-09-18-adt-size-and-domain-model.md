---
slug: adt-size-and-domain-model
title: 代数数据类型的Size和域模型
authors: yjgbg
tags: [adt, functional programing]
---

## 代数数据类型的Size和域模型

[原文地址:https://fsharpforfunandprofit.com/posts/type-size-and-design/](https://fsharpforfunandprofit.com/posts/type-size-and-design/)

在这篇文章中，我们将会学习到如何计算类型的"Size"，或者说是类型的基数。并且了解到这些知识如何帮助我们做出设计选型。

#### 起步

定义类型的Size：通过将数据类型理解为一个Set，我们可以定义数据类型的size为该set中可能出现的元素的总数。

举个简单的例子，boolean类型有两个可能的值，所以```Boolean```类型的Size为2.

有Size为1的数据类型吗？----Unit类型只有一个可能的值: ```()``` .

有Size为0的数据类型吗？有哪个数据类型完全没有任何可能出现的值吗？在F#中没有，但是在Haskell中```Void```是没有可能的值的类型。

下面的这个类型：

``` F#
type ThreeState =
	| Checked
	| Unchecked
	| Unknown
```

这个类型（ThreeState）的Size是多少？ThreeState一共有3个可能的值，所以它的Size是3.

那下面这个类型呢：

```F#
type Direction = 
    | North
    | East
    | South
    | West	
```

显而易见，它(Direction)的Size是4.

我想你明白了Size的意思

#### 计算复合类型的Size

如果你还记得<a herf="https://fsharpforfunandprofit.com/series/understanding-fsharp-types.html">理解F#类型系统</a>系列中提到的：在F#中有两种代数类型："积"类型与"和"类型。积类型有```tuple```和```record```,而和类型在F#被称为“discriminated union"（歧视联合?)。

我们定义```Velocity```类型为一个将```Speed```类型和```Direction```类型联合的record类型:

```F#
type Speed =
    | Slow
    | Fast
    
type Velocity = {
    direction:Direction
    speed:Speed
}
```

那么```Velocity```类型的Size是多少？

下面是每个可能的值：

``` 
{direction=North; speed=Slow}; {direction=North; speed=Fast}
{direction=East;  speed=Slow}; {direction=East;  speed=Fast}
{direction=South; speed=Slow}; {direction=South; speed=Fast}
{direction=West;  speed=Slow}; {direction=West;  speed=Fast}
```

一共有8个可能的值，其中每一个可能都是Speed值与Direction值的联合。

我们将这条规则泛化：

- <strong>定理：积类型的Size为组成该积类型的各个组件类型的Size之积</strong>

```
type RecordType = {
    a : TypeA
    b : TypeB
}
```

Size的计算方式如下:

```F#
size(RecordType) = size(TypeA) * size(TypeB)
```

对于元组而言也是相似的

```F#
type TupleType = TypeA * TypeB
```

这个元组类型的Size:

```F#
size(TupleType) = size(TypeA) * size(TypeB)
```

##### 和类型

如下定义```Movement```

```F#
type Movement =
    | Moving of Direction
    | Nothing
```

我们尝试去写出所有可能的值：

```F#
Moving North
Moving East
Moving South
Moving West
Nothing
```

所以，一共有5个可能的值，```size(Movement) = size(Direction)+1```

下面是一个更有趣的：

```F#
type ThingsYouCanSay =
    | Yes
    | Stop
    | Goodbye
type ThingsICanSay = 
    | No
    | GoGoGo
    | Hello
type HelloGoodbye =
    | YouSay of ThingsYouCanSay
    | ISay of ThingsICanSay
```

我们依然可以写出来所有的可能:

```F#
Yousay Yes
YouSay Stop
YouSay Goodbye
ISay No
ISay GoGoGo
ISay Hello
```

在YouSay中有3个值，在ISay中也有3个值，所有一共有6个可能的值

我们依然可以泛化这条规则：

- <strong>定理:和类型/联合类型的Size是组成该和/联合类型的各个组件的size之和</strong> 

下面是一个联合类型

```
type SumType =
    | CaseA of TypeA
    | CaseB of TypeB
```

SumType的Size如下计算:

```F#
size(SumType) = size(TypeA) + size(TypeB)
```

#### 泛型的类型Size计算

下面这个类型的Size是多少？

```F#
type Optional<'a> = 
    | Something of 'a
    | Nothing
```

首先，我们要知道```Optional<'a>```不是一个<i>类型</i>,而是一个<i>类型构造器</i>。

```Optional<string>```是一个类型，```Optional<int>```也是一个类型，但是```Optional<'a>```并不是一个类型。

然而，我们仍然可以会注意到:size(Optional<string>)正好是```size(string) + 1 ```,```size(Optional<int>)正好是```size(int) + 1,以此类推。

所以我们可以认为:

```F#
size(Optional<'a>) = size('a) + 1
```

类似地，像下面这样有两个泛型的类型:

```F#
type Either<'a,'b> =
    | Left of 'a
    | Right of 'b
```

我们可以通过计算两个泛型组件的Size去计算该类型的Size

```
size(Either<'a,'b>) = size('a) + size('b)
```

#### 递归类型

先来看一个简单的例子，链表：

链表要么是一个空链表，要么是一个二元组（head和tail）。head是一个```'a```类型元素，tail是另一个链表。于是可以定义如下：

```F#
type LinkedList<'a> = 
    | Empty
    | Node of head:'a * tail:LinkedList<'a>
```

为了便于Size的计算，我们简化一下写法

```F#
let S = size(LinkedList<'a>)
let N = size('a)
```

于是：

```F#
S =
    1  //Empty的Size
    + // 联合符号
    N * S // 使用元组的size计算方式计算Node的Size
```

将这个式子简化一点：

``` F#
S = 1 + (N * S)
```

将最后一个S，同样用这个式子表示：

```F#
S = 1 + (N * (1 + (N * S)))
```

化简这个式子，得：

```F#
S = 1 + N + (N^2 * S)
```

再次将最后一个S用这个前面的式子表示:

```F#
S = 1 + N + (N^2 * (1 + (N * S)))
```

化简:

```F#
S = 1+N+N^2+(N^3*S)
```

观察规律发现:

```F#
S = 1 + N + N^2 + N^3 + N^4 + N^5 + ...
```

我们可以从中得出如下结论

- 空列表的size为1
- 只有一个元素的列表的size为 N
- 有两个元素的列表的size为```N*N```
- 有三个元素的列表的size为```N*N*N```
- 以此类推

另一方面，我们也可以尝试通过```S = 1/(1 - N)```(由```S = 1 + (N * S)```变形而来)直接去计算```S```的值，于是：```Direction```类型(size为4）的list的Size为-1/3。。。哈哈，这很奇怪，这让我们想到了<a href="https://www.youtube.com/watch?v=w-I6XTVZXww">自然数之和等于-1/12</a>的那个视频

#### 函数的Size

该如何去计算函数的size 呢？

我们需要做的只有写下来函数的所有可能实现并且数一下，这很简单。

例如，我们有一个叫做```SuitColor```的函数，它可以映射一个```Suit```到一个```Color```，```Black```或者```Red```。

```F#
type Suit = Heart | Spade | Diamond | Club
type Color = Red | Black

type SuitColor = Suit -> Color
```

第一个实现是无论如何返回```Red```：

```F#
(Heart -> Red); (Spade -> Red); (Diamond -> Red); (Club -> Red)
```

另一个实现是除了```Club```返回```Black```，其他都返回```Red```

```F#
(Heart -> Red); (Spade -> Red); (Diamond -> Red); (Club -> Black)
```

我们一共可以写出来16中这个函数的实现:

```F#
(Heart -> Red); (Spade -> Red); (Diamond -> Red); (Club -> Red)
(Heart -> Red); (Spade -> Red); (Diamond -> Red); (Club -> Black)
(Heart -> Red); (Spade -> Red); (Diamond -> Black); (Club -> Red)
(Heart -> Red); (Spade -> Red); (Diamond -> Black); (Club -> Black)

(Heart -> Red); (Spade -> Black); (Diamond -> Red); (Club -> Red)
(Heart -> Red); (Spade -> Black); (Diamond -> Red); (Club -> Black)  // the right one!
(Heart -> Red); (Spade -> Black); (Diamond -> Black); (Club -> Red)
(Heart -> Red); (Spade -> Black); (Diamond -> Black); (Club -> Black)

(Heart -> Black); (Spade -> Red); (Diamond -> Red); (Club -> Red)
(Heart -> Black); (Spade -> Red); (Diamond -> Red); (Club -> Black)
(Heart -> Black); (Spade -> Red); (Diamond -> Black); (Club -> Red)
(Heart -> Black); (Spade -> Red); (Diamond -> Black); (Club -> Black)

(Heart -> Black); (Spade -> Black); (Diamond -> Red); (Club -> Red)
(Heart -> Black); (Spade -> Black); (Diamond -> Red); (Club -> Black)
(Heart -> Black); (Spade -> Black); (Diamond -> Black); (Club -> Red)
(Heart -> Black); (Spade -> Black); (Diamond -> Black); (Club -> Black)
```

另一个计算方式是尝试去定义一个record类型，一个可以用每个值去表示一个特定实现的record类型：哪个返回值对应了输入```Heart```,哪个返回值对应了输入```Spade```。。。

这是一个```SuitColor```对应的类型定义：

```F#
type SuitColorImplementation = {
	Heart : Color
	Spade : Color
	Diamond : Color
	Club : Color
}
```

那么这个record类型的size是多少？

```
size(SuitColorImplementation) = size(Color)
```
